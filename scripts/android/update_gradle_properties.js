const fs = require("fs");

const PLUGIN_NAME = "cordova-plugin-radius-network";
const incrementalKapt = "kapt.incremental.apt=true";
const gradlePropertiesPath = "./platforms/android/gradle.properties";

function log(message) {
  console.log(PLUGIN_NAME + ": " + message);
}

function onError(error) {
  log("ERROR: " + error);
}

function run() {
  let gradleProperties = fs.readFileSync(gradlePropertiesPath);

  if (gradleProperties) {
    let updatedGradleProperties = false;
    gradleProperties = gradleProperties.toString();
    if (!gradleProperties.match(incrementalKapt)) {
      gradleProperties += "\n" + incrementalKapt;
      updatedGradleProperties = true;
    }
    if (updatedGradleProperties) {
      fs.writeFileSync(gradlePropertiesPath, gradleProperties, "utf8");
      log("Updated gradle.properties - set kapt.incremental.apt to true");
    }
  } else {
    log("gradle.properties file not found!");
  }
}

module.exports = function () {
  return new Promise((resolve, reject) => {
    try {
      run();
      resolve();
    } catch (e) {
      onError("EXCEPTION: " + e.toString());
      reject(e);
    }
  });
};
