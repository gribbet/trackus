import * as mapbox from "mapbox-gl";
import Component from "wedges/lib/Component";

import { positionService } from ".";
import { delay, flatten, range } from "./common";

export default class Map extends Component {
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

        const colors = [
            "#f37e79", "#7998f3", "#bbf379", "#f379df", "#79f3e3",
            "#f3bf79", "#9c79f3", "#7af379", "#f3799d", "#79c1f3",
            "#e4f379", "#de79f3", "#79f3ba", "#f39779", "#797ff3",
            "#a2f379", "#f379c6", "#79e9f3", "#f3d979", "#b579f3",
            "#79f392", "#f37984", "#79a8f3", "#cbf379", "#f379ee"
        ]

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
                    "line-color": {
                        "property": "user",
                        "stops": colors.map((color, i) => [i, color])
                    },
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
                    "circle-color": {
                        "property": "user",
                        "stops": colors.map((color, i) => [i, color])
                    },
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
                    flatten(users.map((user, j) => {
                        const current = positionService.current(user);
                        let features: GeoJSON.Feature<any>[] = [];
                        features.push({
                            "type": "Feature",
                            "properties": {
                                "user": j,
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
                                            "user": j,
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