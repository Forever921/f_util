
import 'dart:async';

import 'package:flutter/services.dart';

class FUtil {
  static const MethodChannel _channel =
      const MethodChannel('f_util');

  static Future<String> getAppVersion() async {
    String path= await _channel.invokeMethod("getAppVersion");
    return path;
  }
  static Future<String> getAppName() async {
    String path= await _channel.invokeMethod("getAppName");
    return path;
  }

  static Future<bool> notifyPhoto(String url) async{
    bool isOk= await _channel.invokeMethod("notifyPhoto", { "url": url});
    return isOk;
  }

  static Future<bool> updateApk(String url) async{
    bool isOk= await _channel.invokeMethod("updateApk", { "url": url});
    return isOk;
  }

  static Future<bool> isNotificationEnabled() async{
    bool isOk= await _channel.invokeMethod("isNotificationEnabled");
    return isOk;
  }

  static Future<bool> goToNotificationSetting() async{
    bool isOk= await _channel.invokeMethod("goToNotificationSetting");
    return isOk;
  }

  static Future<bool> setBadge(int num) async{
    bool isOk= await _channel.invokeMethod("setBadge", { "num": num});
    return isOk;
  }


  static void showToast(String message, int duration) =>
      _channel.invokeMethod(
          "showToast", { "message": message, "duration": duration});

  static Future<String> getPhotoPath() async {
    String path= await _channel.invokeMethod("getPhotoPath");
    return path;
  }

  static Future<String> getPhoneModel() async {
    String path= await _channel.invokeMethod("getPhoneModel");
    return path;
  }

  static Future<String> getPhoneVersion() async {
    String path= await _channel.invokeMethod("getPhoneVersion");
    return path;
  }

  static Future<bool> callPhone(String phoneNum) async {
    bool isOk= await _channel.invokeMethod("callPhone",{"phoneNum":phoneNum});
    return isOk;
  }
}
