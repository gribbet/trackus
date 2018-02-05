import * as mapbox from "mapbox-gl/dist/mapbox-gl";
import Application from "wedges/lib/Application";

import { randomString } from "./common";
import Position from "./Position";
import PositionService from "./PositionService";
import StreamSocket from "./StreamSocket";
import Trackus from "./Trackus";

const socketUrl = "wss://api.trackus.grahamgibbons.com/"
const mapboxToken = "pk.eyJ1IjoiZ3JhaGFtYWVyaWFsbGl2ZSIsImEiOiJjaXlnbjZlZmowM3dhMzJyd3BzMXo2am5wIn0.SIOs2eXS97bVJsRoTcuK-w";

(<any>mapbox).accessToken = mapboxToken;

export const positionService = new PositionService();

const user: string = localStorage.getItem("user") || randomString(4);
localStorage.setItem("user", user);

const application = new Application(new Trackus(), document.body);
application.start();

const socket = new StreamSocket<Position>(socketUrl, position => {
    positionService.add(position);
    application.update();
});

window.navigator.geolocation.watchPosition(
    position =>
        socket.send({
            user: user,
            longitude: position.coords.longitude,
            latitude: position.coords.latitude,
            timestamp: position.timestamp
        }),
    error => console.error(error),
    {
        enableHighAccuracy: true
    });