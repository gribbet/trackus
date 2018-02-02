export default class StreamSocket<T> {
    private socket: WebSocket;
    private connected: Promise<void>;

    constructor(url: string, handler: (t: T) => void) {
        this.socket = new WebSocket(url);
        this.connected = new Promise(resolve =>
            this.socket.addEventListener("open", () => resolve()));
        this.socket.addEventListener("message", event =>
            handler(<T>JSON.parse(event.data)));
    }

    async send(t: T): Promise<void> {
        await this.connected;
        this.socket.send(JSON.stringify(t));
    }
}