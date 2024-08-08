const {VCRenderer} = require("../VCRenderer.js")
const {VC, svgTemplate} = require('./VCData.js')
 VCRenderer.renderSVG(VC).then(
    (response) => console.log(response)
);

