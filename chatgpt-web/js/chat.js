
let autoVoiceIdx = 0;
let autoVoiceDataIdx;
let controller;
let controllerId;
let refreshIdx;
let currentResEle;
let progressData = "";
const streamGen = async (long) => {
    controller = new AbortController();
    controllerId = setTimeout(() => {
        notyf.error("请求超时，请稍后重试！一次对话限制处理时间180秒");
        stopLoading();
    }, 180000);
    let headers = {"Content-Type": "application/json"};
    if (customAPIKey) headers["Authorization"] = "Bearer " + customAPIKey;
    let isRefresh = refreshIdx !== void 0;
    if (isRefresh) {
        currentResEle = chatlog.children[systemRole ? refreshIdx - 1 : refreshIdx];
    } else if (!currentResEle) {
        currentResEle = createConvEle("response");
        currentResEle.children[0].innerHTML = "<br />";
        currentResEle.dataset.loading = true;
        scrollToBottom();
    }
    let idx = isRefresh ? refreshIdx : data.length;
    if (existVoice && enableAutoVoice && !long) {
        if (isRefresh) {
            endSpeak();
            autoVoiceDataIdx = currentVoiceIdx = idx;
        } else if (currentVoiceIdx !== data.length) {
            endSpeak();
            autoVoiceDataIdx = currentVoiceIdx = idx;
        }
    }
    let dataSlice;
    if (long) {
        idx = isRefresh ? refreshIdx : data.length - 1;
        dataSlice = [data[idx - 1], data[idx]];
        if (systemRole) {dataSlice.unshift(data[0]);}
    } else if (enableCont) {
        dataSlice = data.slice(0, idx);
    } else {
        dataSlice = [data[idx - 1]];
        if (systemRole) {dataSlice.unshift(data[0]);}
    }
    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                messages: dataSlice,
                model: modelVersion,
                stream: true,
                temperature: roleTemp,
                top_p: roleNature
            }),
            //body: JSON.stringify(dataSlice),
            signal: controller.signal
        });
        clearTimeout(controllerId);
        controllerId = void 0;
        if (res.status !== 200) {
            notyf.error("请求OpenAI异常");
            stopLoading();
            return;
        }
        const reader = res.body.getReader();
        const decoder = new TextDecoder("utf-8");
        const readChunk = async () => {
            const dataChunks = [];

            const processData = async ({ value, done }) => {
                if (!done) {
                    dataChunks.push(value);
                    return reader.read().then(processData);
                }

                const combinedData = new Uint8Array(
                    dataChunks.reduce((acc, curr) => acc.concat(Array.from(curr)), [])
                );
                const decodedData = decoder.decode(combinedData);
                const chunks = decodedData.split(/\n{2}/g).filter((item) => item.trim());

                // 移除最后一个元素
                chunks.pop();

                // 处理分割后的 chunks
                for (let i = 0; i < chunks.length; i++) {
                    let chunk = chunks[i];
                    if (chunk) {
                        let payload = JSON.parse(chunk.slice(6));

                        if (payload.choices[0].finish_reason) {
                            let lenStop = payload.choices[0].finish_reason === "length";
                            let longReplyFlag = enableLongReply && lenStop;
                            if (!enableLongReply && lenStop) {currentResEle.children[1].children[0].className = "halfRefReq"}
                            else {currentResEle.children[1].children[0].className = "refreshReq"};
                            if (existVoice && enableAutoVoice && currentVoiceIdx === autoVoiceDataIdx) {
                                let voiceText = longReplyFlag ? "" : progressData.slice(autoVoiceIdx), stop = !longReplyFlag;
                                autoSpeechEvent(voiceText, currentResEle, false, stop);
                            }
                            break;
                        } else {
                            let content = payload.choices[0].delta.content;
                            if (content) {
                                if (!progressData && !content.trim()) continue;
                                if (existVoice && enableAutoVoice && currentVoiceIdx === autoVoiceDataIdx) {
                                    let spliter = content.match(/\.|\?|!|。|？|！/);
                                    if (spliter) {
                                        let voiceText = progressData.slice(autoVoiceIdx) + content.slice(0, spliter.index + 1);
                                        autoVoiceIdx += voiceText.length;
                                        autoSpeechEvent(voiceText, currentResEle);
                                    }
                                }
                                if (progressData) await delay();
                                progressData += content;
                                currentResEle.children[0].innerHTML = md.render(progressData);
                                if (!isRefresh) {
                                    scrollToBottom();
                                }
                            }
                        }
                    }
                }

                // 处理 done 为 true 的情况
                if (isRefresh) {
                    data[refreshIdx].content = progressData;
                    if (longReplyFlag) return streamGen(true);
                } else {
                    if (long) {data[data.length - 1].content = progressData}
                    else {data.push({role: "assistant", content: progressData})}
                    if (longReplyFlag) return streamGen(true);
                }

                return;
            };

            return reader.read().then(processData);
        };

        await readChunk();
    } catch (e) {
        if (e.message.indexOf("aborted") === -1) {
            notyf.error("访问接口失败，请检查接口！")
        }
        stopLoading();
    } finally {
        stopLoading();
    }
};
const loadAction = (bool) => {
    loading = bool;
    sendBtnEle.disabled = bool;
    sendBtnEle.className = bool ? " loading" : "loaded";
    stopEle.style.display = bool ? "flex" : "none";
    textInputEvent();
}
const stopLoading = (abort = true) => {
    stopEle.style.display = "none";
    if (abort) {
        controller.abort();
        if (controllerId) clearTimeout(controllerId);
        if (delayId) clearTimeout(delayId);
        if (refreshIdx !== void 0) {data[refreshIdx].content = progressData}
        else if (data[data.length - 1].role === "assistant") {data[data.length - 1].content = progressData}
        else {data.push({role: "assistant", content: progressData})}
        if (existVoice && enableAutoVoice && currentVoiceIdx === autoVoiceDataIdx && progressData.length) {
            let voiceText = progressData.slice(autoVoiceIdx);
            autoSpeechEvent(voiceText, currentResEle, false, true);
        }
    }
    updateChats();
    controllerId = delayId = refreshIdx = void 0;
    autoVoiceIdx = 0;
    if(currentResEle != undefined) {
        currentResEle.dataset.loading = false;
        currentResEle = null;
        loadAction(false);

    }

    if(currentResEleImg != undefined) {
        currentResEleImg.dataset.loading = false;
        currentResEleImg = null;
        loadActionImg(false);

    }
    progressData = "";
    loadAction(false);
}
const generateText = async (message) => {
    loadAction(true);
    let requestEle = createConvEle("request");
    requestEle.children[0].innerHTML = md.render(message);
    data.push({role: "user", content: message});
    if (chatsData[activeChatIdx].name === "新的会话") {
        chatsData[activeChatIdx].name = message;
        chatListEle.children[activeChatIdx].children[1].textContent = message;
    }
    updateChats();
    scrollToBottom();
    await streamGen();
};
textarea.onkeydown = (e) => {
    if (e.keyCode === 13 && !e.shiftKey) {
        e.preventDefault();
        genFunc();
    }
};
const genFunc = function () {
    if (recing) {
        toggleRecEv();
    }
    let message = textarea.value.trim();
    if (message.length !== 0) {
        if (loading === true) return;
        textarea.value = "";
        textarea.style.height = "47px";
        generateText(message);
    }
};
sendBtnEle.onclick = genFunc;
stopEle.onclick = stopLoading;