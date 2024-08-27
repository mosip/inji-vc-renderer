const {VCRenderer} = require("../lib/commonJs/vcrenderer.js")
const {VC} = require('./sample-vc.js')
VCRenderer.renderSVG(VC).then(
    (response) => console.log("SVG Image->",response)
);