import Container from "wedges/lib/component/Container";
import Selector from "wedges/lib/component/Selector";
import Styler from "wedges/lib/component/Styler";
import Template from "wedges/lib/component/Template";

import Map from "./Map";

declare var require: (path: string) => string;

export default class Trackus extends Container {

    constructor() {
        super([
            new Template(require("../templates/trackus.pug")),
            new Styler(require("../styles/trackus.pcss")),
            new Selector(".map", new Map())
        ]);
    }
}