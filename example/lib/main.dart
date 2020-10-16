import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:f_util/f_util.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = "APP名字：${await FUtil.getAppName()}\nAPP版本：${await FUtil.getAppVersion()}\n手机型号：${await FUtil.getPhoneModel()}\n手机系统版本：${await FUtil.getPhoneVersion()}\n获取存储图片地址：${await FUtil.getPhotoPath()}\n是否开启通知权限：${await FUtil.isNotificationEnabled()}\n";
    } on PlatformException {
      platformVersion = 'Failed to get platform .';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin app'),
        ),
        body: Column(
          children: [
            Center(
              child: Text('$_platformVersion\n'),
            ),
            Center(
              child: MaterialButton(onPressed: (){
                FUtil.goToNotificationSetting();
              },child: Text("跳转通知设置",style: TextStyle(color: Colors.white),),color: Colors.blue,)
            ),
            Center(
                child: MaterialButton(onPressed: (){
                  FUtil.callPhone('13658034130');
                },child: Text("打电话",style: TextStyle(color: Colors.white),),color: Colors.blue,)
            ),
            Center(
                child: MaterialButton(onPressed: (){
                  FUtil.setBadge(Random().nextInt(100));
                },child: Text("设置badge",style: TextStyle(color: Colors.white),),color: Colors.blue,)
            ),
            Center(
                child: MaterialButton(onPressed: (){
                  // FUtil.updateApk('148338089');
                  FUtil.updateApk('https://rtccsino.oss-cn-hangzhou.aliyuncs.com/2020/10/14/SERatXaNwFEVixU1.apk');
                },child: Text("跳转应用商店",style: TextStyle(color: Colors.white),),color: Colors.blue,)
            ),

          ],
        ),
      ),
    );
  }
}
