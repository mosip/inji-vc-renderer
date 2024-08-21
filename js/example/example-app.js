const {VCRenderer} = require("../VCRenderer.js")
const {VC} = require('./VCData.js')
 VCRenderer.renderSVG(VC).then(
    (response) => console.log("SVG Image-->",response)
);