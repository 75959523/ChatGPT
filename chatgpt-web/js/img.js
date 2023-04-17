
let loadingImg = false;
const textareaImg = document.getElementById("chatinput_img");
const sendBtnEleImg = document.getElementById("sendbutton_img");
let currentResEleImg;
//textareaImg.focus();

const getFuncImg = function () {
    if (recing) {
        toggleRecEv();
    }
    let message = textareaImg.value.trim();
    if (message.length !== 0) {
        if (loading === true) return;
        textareaImg.value = "";
        textareaImg.style.height = "47px";
        generateTextImg(message);
    }
};

const textInputEventImg = () => {
    if (!loadingImg) {
        if (textareaImg.value.trim().length) {
            sendBtnEleImg.classList.add("activeSendBtnImg");
        } else {
            sendBtnEleImg.classList.remove("activeSendBtnImg");
        }
    }
    textareaImg.style.height = "47px";
    textareaImg.style.height = textareaImg.scrollHeight + "px";
};

sendBtnEleImg.onclick = getFuncImg;
textareaImg.oninput = textInputEventImg;

const loadActionImg = (bool) => {
    loading = bool;
    sendBtnEleImg.disabled = bool;
    sendBtnEleImg.className = bool ? " loading" : "loaded";
    stopEle.style.display = bool ? "flex" : "none";
    textInputEventImg();
}

const generateTextImg = async (message) => {
    loadActionImg(true);
    let requestEle = createConvEle("request");
    requestEle.children[0].innerHTML = md.render(message);

    data.push({role: "user", content: message});
    if (chatsData[activeChatIdx].name === "新的会话") {
        chatsData[activeChatIdx].name = message;
        chatListEle.children[activeChatIdx].children[1].textContent = message;
    }

    updateChats();
    scrollToBottom();
    await streamGenImg();
};

const streamGenImg = async (long) => {
    controller = new AbortController();
    controllerId = setTimeout(() => {
        notyf.error("请求超时，请稍后重试！一次对话限制处理时间180秒");
        stopLoading();
    }, 180000);
    let headers = {"Content-Type": "application/json"};
    if (customAPIKey) headers["Authorization"] = "Bearer " + customAPIKey;
    let isRefresh = refreshIdx !== void 0;
    if (isRefresh) {
        currentResEleImg = chatlog.children[systemRole ? refreshIdx - 1 : refreshIdx];
    } else if (!currentResEleImg) {
        currentResEleImg = createConvEle("response");
        currentResEleImg.children[0].innerHTML = "<br />";
        currentResEleImg.dataset.loading = true;
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
    idx = isRefresh ? refreshIdx : data.length - 1;
    dataSlice = data.slice(idx);
    try {
        const res = await fetch(API_URL_IMG, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dataSlice),

            signal: controller.signal
        });
        clearTimeout(controllerId);
        controllerId = void 0;
        if (res.status !== 200) {
            notyf.error("请求OpenAI异常");
            stopLoading();
            return;
        }
        const decoder = new TextDecoder();
        const reader = res.body.getReader();
        const readChunk = async () => {
            return reader.read().then(async ({value, done}) => {
                if (!done) {
                    value = decoder.decode(value);
                    let chunks = value.split(/\n{2}/g);
                    chunks = chunks.filter(item => {
                        return item.trim();
                    });

                    for (let i = 0; i < chunks.length; i++) {

                        const chunk = chunks[i]
                        const strippedStr = chunk.substring(1, chunk.length - 1);
                        const urls = strippedStr.split(', ');

                        urls.forEach(url => {
                            const img = document.createElement('img');
                            img.src = url;
                            img.alt = '图片加载失败';
                            img.style.width = '300px';
                            img.style.height = '300px';
                            img.style.margin = '10px';

                            currentResEleImg.children[0].appendChild(img);
                        });
                    }
                    stopLoading(false);
                }
            });
        };
        await readChunk();
    } catch (e) {
        stopLoading();
    }
};