var helper = require('./helper');

module.exports = function (context) {
  var fs = require('fs');
  var path = require('path');
  var searchFile = function (name, dir) {
    fs.readdir(dir, function (err, files) {
      if (err) {
        return console.error(err);
      } else {
        var tDir = dir;
        for (var i = 0; i < files.length; i++) {
          if (files[i] === name) {
            modifyIt(tDir + "/" + name);
          } else if (files[i].indexOf('.java') === -1) {
            searchFile(name, tDir + "/" + files[i]);
          }
        }

      }
    })
  }

  var modifyIt = function (file) {
    var data = fs.readFileSync(file, 'utf-8');
    data = data.replace('BUGLY_APP_ID', context.opts.cli_variables.ANDROID_APPID);
    data = data.replace('BUGLY_APP_KEY', context.opts.cli_variables.ANDROID_APPKEY);
    fs.writeFile(file, data, 'utf-8', function (err) {
      if (err) {
        return console.log('Insert android bugly access_id and access_key error' + err);
      } else {
        return console.info('Insert android bugly access_id and access_key success');
      }
    });
  }
  var getFileName = function () {
    var configStr = fs.readFileSync("config.xml", "utf-8");
    var tmpStr = configStr.match(/id=\s*([^;]*)/)[0];
    var strs = tmpStr.split(' ')[0].split(".");
    var str = strs[strs.length - 1];
    return str.substr(0, str.length - 1) + "-build-bugly.gradle";
  }
  searchFile(getFileName(), 'platforms/android/cordova-plugin-bugly-sdk');

  helper.restoreRootBuildGradle();
  helper.modifyRootBuildGradle();
}