import * as mapbox from "mapbox-gl/dist/mapbox-gl";
import Application from "wedges/lib/Application";
import Component from "wedges/lib/Component";
import Container from "wedges/lib/component/Container";
import Selector from "wedges/lib/component/Selector";
import Styler from "wedges/lib/component/Styler";
import Template from "wedges/lib/component/Template";

declare var require: (path: string) => string;

class Map extends Component {
    render(element: Element) {
        const map = new mapbox.Map({
            container: element,
            style: {
                "version": 8,
                "center": [-122.446747, 37.733795],
                "zoom": 10,
                "sources": {
                    "base": {
                        "type": "raster",
                        "tiles": ["//server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"],
                        "maxzoom": 19,
                        "tileSize": 128
                    }
                },
                "layers": [
                    {
                        "id": "base",
                        "type": "raster",
                        "source": "base",
                    }
                ]
            }
        });
        return {
            update: () => undefined,
            destroy: () => map.remove()
        }
    }
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

new Application(new Trackus(), document.body).start();