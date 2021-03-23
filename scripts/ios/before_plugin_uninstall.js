const fs = require("fs");
const xcode = require("xcode");
const helper = require("./helper");

module.exports = function(context) {
  const comment = helper.BUILD_PHASE_COMMENT;
  const xcodeProjectPath = helper.getXcodeProjectPath(context);
  const xcodeProject = xcode.project(xcodeProjectPath);
  
  xcodeProject.parseSync();
  
  const buildPhases = xcodeProject.hash.project.objects.PBXShellScriptBuildPhase;
  
  for (var buildPhaseId in buildPhases) {
    const buildPhase = xcodeProject.hash.project.objects.PBXShellScriptBuildPhase[buildPhaseId];
    var shouldDelete = false;
    
    if (buildPhaseId.indexOf("_comment") === -1) {
      shouldDelete = buildPhase.name && buildPhase.name.indexOf(comment) !== -1;
    } 
    else {
      shouldDelete = buildPhaseId === comment;
    }
    
    if (shouldDelete) {
      delete buildPhases[buildPhaseId];
    }
  }
  
  const nativeTargets = xcodeProject.hash.project.objects.PBXNativeTarget;
  
  for (var nativeTargetId in nativeTargets) {
    if (nativeTargetId.indexOf("_comment") === -1) {
      const nativeTarget = nativeTargets[nativeTargetId];
      nativeTarget.buildPhases = nativeTarget.buildPhases.filter(function(buildPhase) {
        return buildPhase.comment !== comment;
      });
    }
  }

  xcodeProject.removeFromBuildSettings('VALIDATE_WORKSPACE');

  fs.writeFileSync(xcodeProjectPath, xcodeProject.writeSync());
};
