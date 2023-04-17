
const socket = new WebSocket('ws://localhost:8080/ws');

socket.addEventListener('open', (event) => {
    console.log('WebSocket connection opened:', event);
    // 发送数据到服务器
    socket.send('Hello, WebSocket server!');
});

socket.addEventListener('message', (event) => {
    console.log('WebSocket message received:', event.data);
    // 处理服务器返回的数据
});

socket.addEventListener('close', (event) => {
    console.log('WebSocket connection closed:', event);
});

socket.addEventListener('error', (event) => {
    console.log('WebSocket error:', event);
});