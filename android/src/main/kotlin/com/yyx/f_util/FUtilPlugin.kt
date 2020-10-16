package com.yyx.f_util

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** FUtilPlugin */
class FUtilPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity

  private lateinit var context: Context
  private lateinit var channel: MethodChannel

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    this.context = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "f_util")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }


  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

    when (call.method) {

      "getAppVersion" -> {
        result.success(AppUtils.getVersionName(context))
      }

      "getAppName" -> {
        result.success(AppUtils.getAppName(context))
      }
      
      "getPhoneModel" -> {
        result.success(Build.MODEL)
      }
      
      "getPhoneVersion" -> {
        result.success("Android" + Build.VERSION.RELEASE)
      }

      "notifyPhoto" -> {
        val url: String = call.argument<String>("url")!!
        val image = File(url)
        val resolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Image" + System.currentTimeMillis())
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, AppUtils.getAppName(context))
        contentValues.put(MediaStore.Images.Media.TITLE, AppUtils.getAppName(context))
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
          contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis().toString())
        val uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        try {
          inputStream = image.inputStream()
          outputStream = resolver?.openOutputStream(uri!!)
          val buffer = ByteArray(1024)
          var byteCount = 0
          while (inputStream.read(buffer).also { byteCount = it } != -1) {
            outputStream?.write(buffer, 0, byteCount)
          }
          outputStream?.flush()
          outputStream?.close()
          inputStream.close()
          result.success(true)
        } catch (e: Exception) {
          outputStream?.close()
          inputStream?.close()
          result.success(false)
        }
      }
      "getPhotoPath" -> {
        result.success(context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path + File.separator)
      }
      "showToast" -> {
        showToast(context, call.argument<String>("message")!!, call.argument<Int>("duration")!!)
        result.success(null)
      }
      "updateApk" -> {
        val url = call.argument<String>("url")
        updateApk(url, result)
      }
      "setBadge" -> {
        val num = call.argument<Int>("num")
        num?.let {
          try {
            result.success(BadgeUtils.setCount(it, context))
          } catch (e: Exception) {
            result.success(false)
          }
        }
      }
      "isNotificationEnabled" -> {
        result.success(isNotificationEnabled(context))
      }
      "goToNotificationSetting" -> {
        goToNotificationSetting(context)
        result.success(true)
      }
      "callPhone" -> {
        val phoneNum = call.argument<String>("phoneNum")
        phoneNum?.let {
          callPhone(context, it)
        }
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun callPhone(context: Context, phoneNum: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phoneNum")
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
  }

  /**
   * 获取通知权限
   * @param context
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  private fun isNotificationEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= 24) {
      return NotificationManagerCompat.from(context).areNotificationsEnabled();
    } else if (Build.VERSION.SDK_INT >= 19) {
      val CHECK_OP_NO_THROW = "checkOpNoThrow"
      val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
      val mAppOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
      val appInfo = context.applicationInfo
      val pkg = context.applicationContext.packageName
      val uid = appInfo.uid
      var appOpsClass: Class<*>? = null
      try {
        appOpsClass = Class.forName(AppOpsManager::class.java.name)
        val checkOpNoThrowMethod = appOpsClass!!.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                String::class.java)
        val opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
        val value = opPostNotificationValue.get(Int::class.java) as Int
        return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return false
  }

  /**
   * 跳到通知栏设置界面
   * @param context
   */
  private fun goToNotificationSetting(context: Context) {
    //直接跳转到应用通知设置的代码：
    val localIntent = Intent()
    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
    localIntent.data = Uri.fromParts("package", context.packageName, null)
    context.startActivity(localIntent)
  }


  private fun showToast(context: Context, message: String, duration: Int) = Toast.makeText(context, message, duration).show()

  private fun updateApk(downloadUrl: String?, result: Result) {
    try {
      //PREPARE URLS
      val destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path+ File.separator + "update_app.apk"
      val fileUri = Uri.parse("file://$destination")

      //DELETE APK FILE IF SOME ALREADY EXISTS
      val file = File(destination)
      if (file.exists()) {
        if (!file.delete()) {
          Log.e("updateApk", "ERROR: unable to delete old apk file before starting OTA")
        }
      }
      //CREATE DOWNLOAD MANAGER REQUEST
      val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(downloadUrl))
      request.setTitle(AppUtils.getAppName(context) + "APP更新")
      request.setDestinationUri(fileUri)

      //GET DOWNLOAD SERVICE AND ENQUEUE OUR DOWNLOAD REQUEST
      val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
      val downloadId = manager.enqueue(request)

      //START TRACKING DOWNLOAD PROGRESS IN SEPARATE THREAD
//            trackDownloadProgress(downloadId, manager)

      //REGISTER LISTENER TO KNOW WHEN DOWNLOAD IS COMPLETE
      context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
          //DOWNLOAD IS COMPLETE, UNREGISTER RECEIVER AND CLOSE PROGRESS SINK
          context.unregisterReceiver(this)
          //TRIGGER APK INSTALLATION
          val intent: Intent
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //AUTHORITY NEEDS TO BE THE SAME ALSO IN MANIFEST
            val apkUri = FileProvider.getUriForFile(context, AppUtils.getPackageName(context) + ".fileprovider", File(destination))
            intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          } else {
            intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          //SEND INSTALLING EVENT
          context.startActivity(intent)
          result.success(true)
        }
      }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    } catch (e: Exception) {
      result.success(false)
    }
  }
}
