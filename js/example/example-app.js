const {VCRenderer} = require("../dist/injivcrenderer.bundle")
const {VC} = require('./sample-vc.js')
 VCRenderer.renderSVG(VC).then(
    (response) => console.log("SVG Image-->",response)
);