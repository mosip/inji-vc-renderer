const {VCRenderer} = require("../lib/commonJs/vcrenderer.js")
const {InsuranceVC, MosipVCWithQR, MosipVCWithoutQR} = require('./sample-vc.js')
VCRenderer.renderSVG(MosipVCWithoutQR).then(
    (response) => console.log("::::::::::::::SVG Image->",response)
);