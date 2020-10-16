#import "FUtilPlugin.h"
#if __has_include(<f_util/f_util-Swift.h>)
#import <f_util/f_util-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "f_util-Swift.h"
#endif

@implementation FUtilPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFUtilPlugin registerWithRegistrar:registrar];
}
@end
