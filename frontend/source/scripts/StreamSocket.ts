export default class StreamSocket<T> {
    private socket: WebSocket;
    private connected: Promise<void>;

    constructor(
        private url: string,
        private handler: (t: T) => void) {

        this.connect();
    }

    private connect() {
        this.socket = new WebSocket(this.url);
        this.connected = new Promise(resolve =>
            this.socket.addEventListener("open", () => (
                console.log("Connected"),
                resolve())));
        this.socket.addEventListener("message", event =>
            this.handler(<T>JSON.parse(event.data)));
        this.socket.addEventListener("close", () => this.connect());
    }

    async send(t: T): Promise<void> {
        await this.connected;
        this.socket.send(JSON.stringify(t));
    }
}