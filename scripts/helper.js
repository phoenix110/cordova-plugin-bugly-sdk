var fs = require("fs");
var path = require("path");

function rootBuildGradleExists () {
  var target = path.join("platforms", "android", "build.gradle");
  return fs.existsSync(target);
}

/*
 * Helper function to read the build.gradle that sits at the root of the project
 */
function readRootBuildGradle () {
  var target = path.join("platforms", "android", "build.gradle");
  return fs.readFileSync(target, "utf-8");
}


function addDependencies (buildGradle) {
  // find the known line to match
  var match = buildGradle.match(/^(\s*)classpath 'com.android.tools.build(.*)/m);
  var whitespace = match[1];
  console.log('------------------------------------');
  // modify the line to add the necessary dependencies
  var googlePlayDependency = whitespace + 'classpath \'com.tencent.bugly:symtabfileuploader:latest.release\' // symtabfileuploader dependency from cordova-plugin-bugly';
  var modifiedLine = match[0] + '\n' + googlePlayDependency;
  console.log('********************');
  console.log(modifiedLine);
  // modify the actual line
  return buildGradle.replace(/^(\s*)classpath 'com.android.tools.build(.*)/m, modifiedLine);
}


/*
 * Helper function to write to the build.gradle that sits at the root of the project
 */
function writeRootBuildGradle (contents) {
  var target = path.join("platforms", "android", "build.gradle");
  fs.writeFileSync(target, contents);
}

module.exports = {

  modifyRootBuildGradle: function () {
    // be defensive and don't crash if the file doesn't exist
    if (!rootBuildGradleExists) {
      return;
    }

    var buildGradle = readRootBuildGradle();

    // Add Google Play Services Dependency
    buildGradle = addDependencies(buildGradle);

    // Add Google's Maven Repo
    // buildGradle = addRepos(buildGradle);

    writeRootBuildGradle(buildGradle);
  },

  restoreRootBuildGradle: function () {
    // be defensive and don't crash if the file doesn't exist
    if (!rootBuildGradleExists) {
      return;
    }

    var buildGradle = readRootBuildGradle();

    // remove any lines we added
    buildGradle = buildGradle.replace(/(?:^|\r?\n)(.*)cordova-plugin-bugly*?(?=$|\r?\n)/g, '');

    writeRootBuildGradle(buildGradle);
  }
};