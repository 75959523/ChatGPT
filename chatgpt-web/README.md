# chatgpt-web
Pure Javascript ChatGPT demo based on OpenAI API (gpt-3.5-turbo)

纯JS实现的ChatGPT项目，基于OpenAI gpt-3.5-turbo API

部署一个HTML文件即可使用。

支持复制，刷新，语音输入，朗读等功能，以及众多[自定义选项](#自定义选项)。

参考项目: 
[markdown-it](https://github.com/markdown-it/markdown-it), 
[highlight.js](https://github.com/highlightjs/highlight.js), 
[github-markdown-css](https://github.com/sindresorhus/github-markdown-css), 
[chatgpt-html](https://github.com/slippersheepig/chatgpt-html), 
[markdown-it-copy](https://github.com/ReAlign/markdown-it-copy), 
[markdown-it-texmath](https://github.com/goessner/markdown-it-texmath), 
[awesome-chatgpt-prompts-zh](https://github.com/PlexPt/awesome-chatgpt-prompts-zh)

![示例](https://github.com/xqdoo00o/chatgpt-web/blob/main/example.png)

## Demo

[在线预览](https://xqdoo00o.github.io/chatgpt-web/) （使用需配置自定义API key和自定义接口）

## 使用方法
1. 任意HTTP Server部署index.html。打开设置，填写自定义API Key。选择自定义接口

    (1). 可正常访问`api.openai.com`，填写`https://api.openai.com/`

    (2). 不可正常访问`api.openai.com`，填写其反代地址（可使用cloudflare worker等反代），注意：反代地址以 `/` 结尾，需添加跨域Header `Access-Control-Allow-Origin`
2. 配合nginx反代使用, 示例配置如下。

    **注意：服务器需正常访问`api.openai.com`**
```
server {
    listen       80;
    server_name  example.com;
    #开启openai接口的gzip压缩，大量重复文本的压缩率高，节省服务端流量
    gzip  on;
    gzip_min_length 1k;
    gzip_types text/event-stream;

    #如需部署在网站子路径，如"example.com/chatgpt"，配置如下
    #location ^~ /chatgpt/v1 {
    location ^~ /v1 {
        proxy_pass https://api.openai.com/v1;
        proxy_set_header Host api.openai.com;
        proxy_ssl_name api.openai.com;
        proxy_ssl_server_name on;
        #注意Bearer 后改为正确的token。如需用户自定义API key，可注释掉下一行
        proxy_set_header  Authorization "Bearer sk-your-token";
        proxy_pass_header Authorization;
        #流式传输，不关闭buffering缓存会卡顿卡死，必须配置！！！
        proxy_buffering off;
    }
    #与上面反代接口的路径保持一致
    #location /chatgpt {
    location / {
        alias /usr/share/nginx/html/;
        index index.html;
    }
}
```
如服务器无法正常访问`api.openai.com`, 可配合socat反代和http代理使用，proxy_pass配置改成
```
proxy_pass https://127.0.0.1:8443/v1;
```
并打开socat
```
socat TCP4-LISTEN:8443,reuseaddr,fork PROXY:http代理地址:api.openai.com:443,proxyport=http代理端口
```
2. 配合Caddy反代使用，可以自动生产HTTPS证书。

   **注意：服务器需正常访问`api.openai.com`**

   **Caddy 2.6.5及之后版本支持https_proxy和http_proxy环境变量，如服务器无法正常访问`api.openai.com`，可先设置代理环境变量**
```
yourdomain.example.com {
	reverse_proxy /v1/* https://api.openai.com {
		header_up Host api.openai.com
		header_up Authorization "{http.request.header.Authorization}"
		header_up Authorization "Bearer sk-your-token"
	}

	file_server / {
		root /var/wwwroot/chatgpt-web
		index index.html
	}
}
```

## 自定义选项

1. 可选GPT模型，默认gpt-3.5，当前使用gpt-4模型需通过openai的表单申请。

2. 可选自定义接口地址，配合nginx和caddy反代可以不配置。

3. 可选API key，默认不设置，如需用户自定义API key使用，建议Nginx一定要配置https，公网以http方式明文传输API key极易被中间人截获。

4. 可选系统角色，默认不开启，有三个预设角色，并动态加载[awesome-chatgpt-prompts-zh](https://github.com/PlexPt/awesome-chatgpt-prompts-zh)中的角色。

5. 可选角色性格，默认灵活创新，对应接口文档的top_p参数。

6. 可选回答质量，默认平衡，对应接口文档的temperature参数。

7. 修改打字机速度，默认较快，值越大速度越快。

8. 允许连续对话，默认开启，对话中包含上下文信息，会导致api费用增加。

9. 允许长回复，默认关闭，开启后可能导致api费用增加，并丢失大部分上下文，对于一些要发送`继续`才完整的回复，不用发`继续`了。

10. 选择语音，默认Bing语音，支持Azure语音和系统语音，可分开设置提问语音和回答语音。

11. 音量，默认最大。

12. 语速，默认正常。

13. 音调，默认正常。

14. 允许连续朗读，默认开启，连续郎读到所有对话结束。

15. 允许自动朗读，默认关闭，自动朗读新的回答。（iOS需打开设置-自动播放视频预览，Mac上Safari需打开此网站的设置-允许全部自动播放）

16. 支持语音输入，默认识别为普通话，可长按语音按钮修改识别选项。如浏览器不支持语音输入，则不显示语音按钮（chrome内核浏览器+https网页体验最佳）。如点击语音按钮没反应，则未允许麦克风权限，或者没安装麦克风设备。

17. 左边栏支持功能，新建会话，重命名，删除会话。导出所有会话，导入会话文件，清空所有会话。