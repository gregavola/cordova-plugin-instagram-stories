
var exec = require('cordova/exec');

var PLUGIN_NAME = 'IGStory';

var IGStory = {
  shareToStory: function(opts, cb, err) {
    exec(cb, err, PLUGIN_NAME, 'shareToStory', [opts.backgroundImage, opts.stickerImage, opts.attributionURL, opts.backgroundTopColor, opts.backgroundBottomColor]);
  }
};

module.exports = IGStory;
