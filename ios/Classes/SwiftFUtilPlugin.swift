import Flutter
import UIKit

public class SwiftFUtilPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "f_util", binaryMessenger: registrar.messenger())
    let instance = SwiftFUtilPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
    switch call.method {
    case "getAppVersion":
        let appVersion = Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String
        result(appVersion)
        break;
    case "getAppName":
        let appName = Bundle.main.infoDictionary!["CFBundleDisplayName"] as! String
        result(appName)
        break;
    case "getPhotoPath":
        result(NSTemporaryDirectory()+"photo/")
        break
    case "getPhoneVersion":
        result("iOS"+UIDevice.current.systemVersion)
        break
    case "getPhoneModel":
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        result(identifier)
        break
    case "notifyPhoto":
        let args = call.arguments as! NSDictionary
        let url = args["url"] as! String
        loadImage(image: UIImage(contentsOfFile: url)!, result: result)
        break
    case "showToast":
        let args = call.arguments as! NSDictionary
        let message = args["message"] as! String
        print(message)
        break
    case "updateApk":
        let args = call.arguments as! NSDictionary
        let urlString = args["url"] as! String
        let url =  URL(string:"itms-apps://itunes.apple.com/app/id"+urlString)
        if(UIApplication.shared.canOpenURL(url!)){
            UIApplication.shared.openURL(url!)
            result(true)
        }else{
            result(false)
        }
        break
    case "isNotificationEnabled":
        //开启用户权限，否则不会显示
        if #available(iOS 10.0, *) {
           UNUserNotificationCenter.current().getNotificationSettings(){(setttings) in
                       switch setttings.authorizationStatus{
                       case .authorized:
                        result(true)
                        break
                       case .denied:
                        result(false)
                        break
                       case .notDetermined:
                        result(false)
                        break
                       case .provisional:
                        result(false)
                        break
                       @unknown default:
                        result(false)
                        break
                       }
                   }
        } else {
        let isNotificationEnabled = UIApplication.shared.currentUserNotificationSettings?.types.contains(UIUserNotificationType.alert)
            result(isNotificationEnabled)
        }
        break
    case "goToNotificationSetting":
        let url = URL(string: UIApplication.openSettingsURLString)
        if(UIApplication.shared.canOpenURL(url!)){
            UIApplication.shared.openURL(url!)
            result(true)
        }else{
            result(false)
        }
        break
    case "setBadge":
        let args = call.arguments as! NSDictionary
        let num = args["num"] as! NSNumber
        //开启用户权限，否则不会显示
        let version = UIDevice.current.systemVersion
        if(Float(version)!>=8.0){
            let set = UIUserNotificationSettings(types:[.alert,.badge,.sound], categories: nil)
           UIApplication.shared.registerUserNotificationSettings(set)
           UIApplication.shared.registerForRemoteNotifications()
        }
        //设置角标数字
        UIApplication.shared.applicationIconBadgeNumber=num.intValue
        break
    case "callPhone":
        let args = call.arguments as! NSDictionary
        let phoneNum = args["phoneNum"] as! String
        let url = URL(string: "tel:"+phoneNum)
        if(UIApplication.shared.canOpenURL(url!)){
            UIApplication.shared.openURL(url!)
            result(true)
        }else{
            result(false)
        }
        break
    default:
        result("notImplemented");
        break
    }
  }
    func loadImage(image:UIImage,result:FlutterResult) {
        UIImageWriteToSavedPhotosAlbum(image, self, #selector(saveImage(image:didFinishSavingWithError:contextInfo:result:)), nil)
    }
        
    @objc private func saveImage(image: UIImage, didFinishSavingWithError error: NSError?, contextInfo: AnyObject,result:FlutterResult) {
        if error != nil{
            result(false)
        }else{
            result(true)
        }
    }
    
}
