"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.fetchTemplate = fetchTemplate;
exports.getValueBasedOnLanguage = getValueBasedOnLanguage;
function fetchTemplate(url) {
  return fetch(url).then(response => {
    if (!response.ok) {
      throw new Error(`Unexpected response status: ${response.status}`);
    }
    const contentType = response.headers.get('Content-Type');
    if (contentType !== 'image/svg+xml') {
      throw new Error(`Expected image/svg+xml but received ${contentType}`);
    }
    return response.text().then(body => {
      if (!body) {
        throw new Error('Empty response body');
      }
      return body;
    });
  }).catch(error => {
    console.error('Error fetching SVG:', error);
    return '';
  });
}
function getValueBasedOnLanguage(arrayOfObjects, language) {
  for (const jsonObject of arrayOfObjects) {
    if (jsonObject.language === language) {
      return jsonObject.value || "";
    }
  }
  return "";
}
//# sourceMappingURL=utils.js.map