# camera-plugin
Multiple snapshots apache cordova camera plugin

This is an apache cordova plugin designed for android platform that allows you to take multiple snapshots and render them 
in an unique .pdf file, this would be useful in case you want to have in a document several invoices for example.

<p align="center">
  <img src="camera-plugin.jpg" />
</p>

<table>
  <tr>
    <td><img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/wwleak/camera-plugin?style=for-the-badge"></td>
    <td><img alt="GitHub top language" src="https://img.shields.io/github/languages/top/wwleak/camera-plugin?style=for-the-badge"></td>
    <td><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/wwleak/camera-plugin?style=for-the-badge"></td>
    <td><img alt="GitHub issues" src="https://img.shields.io/github/issues/wwleak/camera-plugin?style=for-the-badge"></td>
  </tr>
</table>

The integration with phonegap or ionic framework is pretty straight forward, you just have to install the plugin via cli 
this way:

First, generate the destination platform

```console
foo@bar:~$ ionic platform add android
```
Finally, install the plugin

```console
foo@bar:~$ ionic plugin add https://github.com/wwleak/camera-plugin.git
```

And that's it.
