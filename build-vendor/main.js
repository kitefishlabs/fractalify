(function () {

  var React = require('react'),
    injectTapEventPlugin = require("react-tap-event-plugin"),
    materialUI = require('material-ui'),
    reactSocial = require('react-social'),
    colorPicker = require('react-colors-picker');

  require('./main.css');

  //Needed for React Developer Tools
  window.React = React;
  window.MaterialUI = materialUI;
  window.ColorPicker = colorPicker;
  window.ReactSocial = reactSocial;

  //Needed for onTouchTap
  //Can go away when react 1.0 release
  //Check this repo:
  //https://github.com/zilverline/react-tap-event-plugin
  injectTapEventPlugin();
 
})();
