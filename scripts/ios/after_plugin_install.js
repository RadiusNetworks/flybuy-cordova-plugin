const fs = require("fs");
const xcode = require("xcode");
const helper = require("./helper");

module.exports = function(context) {
  const comment = helper.BUILD_PHASE_COMMENT;
  const xcodeProjectPath = helper.getXcodeProjectPath(context);
  const xcodeProject = xcode.project(xcodeProjectPath);
  
  xcodeProject.parseSync();
  
  const buildPhase = xcodeProject.pbxItemByComment(comment, "PBXShellScriptBuildPhase");
  
  if (!buildPhase) {
    const result = xcodeProject.addBuildPhase([], "PBXShellScriptBuildPhase", comment, null, {
      shellPath: "/bin/sh",
      shellScript: 'FB_BIN="${TARGET_BUILD_DIR}/${WRAPPER_NAME}/Frameworks/FlyBuy.xcframework/FlyBuy"; lipo -remove x86_64 "$FB_BIN" -o "$FB_BIN";',
    });

    // Fixes build issue on Xcode 12.3.
    xcodeProject.addToBuildSettings('VALIDATE_WORKSPACE', 'NO');

    result.buildPhase.runOnlyForDeploymentPostprocessing = 1;
    
    fs.writeFileSync(xcodeProjectPath, xcodeProject.writeSync());
  }
};
