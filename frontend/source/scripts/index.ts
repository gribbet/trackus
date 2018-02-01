import * as mapbox from "mapbox-gl/dist/mapbox-gl";
import { GeoJSONGeometry } from "mapbox-gl/dist/mapbox-gl";
import Application from "wedges/lib/Application";
import Component from "wedges/lib/Component";
import Container from "wedges/lib/component/Container";
import Selector from "wedges/lib/component/Selector";
import Styler from "wedges/lib/component/Styler";
import Template from "wedges/lib/component/Template";

declare var require: (path: string) => string;

export function range(start: number, end: number): number[] {
    return new Array(end - start)
        .fill(0)
        .map((_, i) => i + start);
}

export function flatten<T>(x: T[][]): T[] {
    return [].concat.apply([], x);
}

interface UserId extends String { }

interface Position {
    user: UserId;
    longitude: number;
    latitude: number;
    timestamp: number;
}

class PositionService {

    private map: {
        [id: string]: Position[];
    } = {};

    add(position: Position) {
        let index = this.findIndex(
            position.user,
            position.timestamp);
        this.positions(position.user).splice(
            index,
            0,
            position);
    }

    slice(user: UserId, start: number, end: number): Position[] {
        let startIndex = this.findIndex(user, end);
        let endIndex = this.findIndex(user, start);
        const positions = this.positions(user);

        if (startIndex === -1) startIndex = positions.length - 1;
        if (startIndex > 0) startIndex = startIndex - 1;
        if (endIndex === -1) endIndex = positions.length;

        return positions.slice(startIndex, endIndex);
    }

    users(): UserId[] {
        return Object.keys(this.map);
    }

    current(user: UserId): Position {
        return this.positions(user)[0];
    }

    private positions(user: UserId): Position[] {
        const id = user.toString();
        return this.map[id] =
            this.map[id] || [];
    }

    private findIndex(user: UserId, timestamp: number): number {
        const positions = this.positions(user);
        let index = positions
            .findIndex(_ => _.timestamp < timestamp);
        if (index === -1)
            index = positions.length - 1;
        return index;
    }
}

const positionService = new PositionService();

(<any>mapbox).accessToken = "pk.eyJ1IjoiZ3JhaGFtYWVyaWFsbGl2ZSIsImEiOiJjaXlnbjZlZmowM3dhMzJyd3BzMXo2am5wIn0.SIOs2eXS97bVJsRoTcuK-w";

class Map extends Component {
    private maps: mapbox.Map[] = [];

    render(element: Element) {
        const map = new mapbox.Map({
            container: element,
            style: "mapbox://styles/mapbox/light-v9"
        });
        this.maps.push(map);

        const outline = 0.2;
        const min = 4;
        const max = 25;
        const start = 8;
        const stop = 20;

        map.on("load", () => {
            map.addSource("data", {
                "type": "geojson",
                "data": {
                    "type": "FeatureCollection",
                    "features": []
                }
            });
            map.addLayer({
                "id": "lines",
                "source": "data",
                "type": "line",
                "layout": {
                    "line-cap": "round",
                    "line-join": "round"
                },
                "paint": {
                    "line-color": "#58e8cb",
                    "line-width": ["interpolate", ["linear"], ["zoom"],
                        start, ["*", ["*", ["get", "factor"], ["get", "factor"]], min],
                        stop, ["*", ["*", ["get", "factor"], ["get", "factor"]], max]
                    ]
                }
            });
            map.addLayer({
                "id": "current-outline",
                "source": "data",
                "type": "circle",
                "filter": ["==", ["get", "current"], true],
                "paint": {
                    "circle-color": "#000",
                    "circle-radius": ["interpolate", ["linear"], ["zoom"],
                        start, min / 2 * (1 + outline),
                        stop, max / 2 * (1 + outline)
                    ]
                }
            });
            map.addLayer({
                "id": "current",
                "source": "data",
                "type": "circle",
                "filter": ["==", ["get", "current"], true],
                "paint": {
                    "circle-color": "#58e8cb",
                    "circle-radius": ["interpolate", ["linear"], ["zoom"],
                        start, min / 2,
                        stop, max / 2
                    ]
                }
            });
        });
        map.setZoom(12);
        map.setCenter([-122.446747, 37.733795]);
        return {
            update: () => {
                const data = <mapbox.GeoJSONSource>map.getSource("data");

                const users = positionService.users();

                const segments = 20;
                const duration = 5 * 60 * 1000;

                const features =
                    flatten(users.map(user => {
                        const current = positionService.current(user);
                        let features: GeoJSON.Feature<GeoJSONGeometry>[] = [];
                        features.push({
                            "type": "Feature",
                            "properties": {
                                "user": user,
                                "current": true
                            },
                            "geometry": {
                                "type": "Point",
                                "coordinates": [current.longitude, current.latitude]
                            }
                        });
                        features = features.concat(
                            range(0, segments)
                                .map(i => {
                                    const start = current.timestamp - duration + i * duration / segments;
                                    const end = start + duration / segments;
                                    const positions = positionService.slice(user, start, end);
                                    return <GeoJSON.Feature<GeoJSON.LineString>>{
                                        "type": "Feature",
                                        "properties": {
                                            "user": user,
                                            "factor": (i + 1) / segments,
                                        },
                                        "geometry": {
                                            "type": "LineString",
                                            "coordinates": positions.map(_ =>
                                                [_.longitude, _.latitude])
                                        }
                                    };
                                }));
                        return features;
                    }));
                data.setData({
                    "type": "FeatureCollection",
                    "features": features
                });
            },
            destroy: () => {
                map.remove();
                this.maps = this.maps.filter(_ => _ !== map);
            }
        }
    }

    async load() {
        const check = async (map: mapbox.Map) => {
            if (map.loaded())
                return true;
            await delay(50);
            await check(map);
        };
        await Promise.all(this.maps.map(_ => check(_)));
    }
}

export function delay(time: number): Promise<void> {
    return new Promise(_ => setTimeout(_, time))
}


class Trackus extends Container {

    constructor() {
        super([
            new Template(require("../templates/trackus.pug")),
            new Styler(require("../styles/trackus.pcss")),
            new Selector(".map", new Map())
        ]);
    }
}

const application = new Application(new Trackus(), document.body);
application.start();

const user: string = "test";

/*range(0, 60)
    .forEach(i =>
        positionService.add({
            user: user,
            longitude: -122.4157053 + 0.01 * (i / 60),
            latitude: 37.756119 + 0.01 * (i / 60) * (i / 60),
            timestamp: new Date().getTime() - i * 1000
        }));*/



application.update();


window.navigator.geolocation.watchPosition(
    position =>
        application.update(() =>
            positionService.add({
                user: user,
                longitude: position.coords.longitude,
                latitude: position.coords.latitude,
                timestamp: position.timestamp
            })),
    error => console.log(error),
    {
        enableHighAccuracy: true
    });