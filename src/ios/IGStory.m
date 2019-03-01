#import "IGStory.h"

@implementation IGStory

@synthesize callbackId;

- (void)pluginInitialize {
    
}

- (void)shareToStory:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;

    NSString* backgroundImage = [command.arguments objectAtIndex:0];
    NSString* stickerImage = [command.arguments objectAtIndex:1];
    NSString* attributionURL = [command.arguments objectAtIndex:2];
    NSString* backgroundTopColor = [command.arguments objectAtIndex:3];
    NSString* backgroundBottomColor = [command.arguments objectAtIndex:4];

    NSLog(@"This is backgroundURL: %@", backgroundImage);
    NSLog(@"This is stickerURL: %@", stickerImage);

    if ([backgroundTopColor length] != 0  && [backgroundBottomColor length] != 0) {
        NSURL *stickerImageURL = [NSURL URLWithString:stickerImage];
        
        NSError *stickerImageError;
        NSData* stickerData = [NSData dataWithContentsOfURL:stickerImageURL options:NSDataReadingUncached error:&stickerImageError];
        
        if (stickerData && !stickerImageError) {
            [self shareColorAndStickerImage:backgroundTopColor backgroundBottomColor:backgroundBottomColor stickerImage:stickerData  attributionURL:attributionURL commandId: command.callbackId];
        } else {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing Sticker background"];
            dispatch_async(dispatch_get_main_queue(), ^{
                [self finishCommandWithResult:result commandId: command.callbackId];
            });
        }
        
    } else {
        NSURL *stickerImageURL = [NSURL URLWithString:stickerImage];
        NSURL *backgroundImageURL = [NSURL URLWithString:backgroundImage];
        
        NSError *backgroundImageError;
        NSData* imageDataBackground = [NSData dataWithContentsOfURL:backgroundImageURL options:NSDataReadingUncached error:&backgroundImageError];
        
        if (imageDataBackground && !backgroundImageError) {
            NSError *stickerImageError;
            NSData* stickerData = [NSData dataWithContentsOfURL:stickerImageURL options:NSDataReadingUncached error:&stickerImageError];
            
            if (stickerData && !stickerImageError) {
                [self shareBackgroundAndStickerImage:imageDataBackground stickerImage:stickerData  attributionURL:attributionURL commandId: command.callbackId];
            } else {
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing Sticker background"];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self finishCommandWithResult:result commandId: command.callbackId];
                });
            }
        } else {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing Image background"];
            dispatch_async(dispatch_get_main_queue(), ^{
                [self finishCommandWithResult:result commandId: command.callbackId];
            });
        }
    }

    
}

- (void)shareBackgroundAndStickerImage:(NSData *)backgroundImage stickerImage:(NSData *)stickerImage attributionURL:(NSString *)attributionURL commandId:(NSString *)command  {

    // Verify app can open custom URL scheme. If able,
    // assign assets to pasteboard, open scheme.
    NSURL *urlScheme = [NSURL URLWithString:@"instagram-stories://share"];
    if ([[UIApplication sharedApplication] canOpenURL:urlScheme]) {
      
      NSLog(@"IG IS AVAIALBLE");

      // Assign background and sticker image assets and
      // attribution link URL to pasteboard
      NSArray *pasteboardItems = @[@{@"com.instagram.sharedSticker.backgroundImage" : backgroundImage,
                                     @"com.instagram.sharedSticker.stickerImage" : stickerImage,
                                     @"com.instagram.sharedSticker.contentURL" : attributionURL}];
      NSDictionary *pasteboardOptions = @{UIPasteboardOptionExpirationDate : [[NSDate date] dateByAddingTimeInterval:60 * 5]};
      // This call is iOS 10+, can use 'setItems' depending on what versions you support
      [[UIPasteboard generalPasteboard] setItems:pasteboardItems options:pasteboardOptions];

      [[UIApplication sharedApplication] openURL:urlScheme options:@{} completionHandler:nil];

      NSDictionary *payload = [NSDictionary dictionaryWithObjectsAndKeys:attributionURL, @"url", nil];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                        messageAsDictionary:payload];

      dispatch_async(dispatch_get_main_queue(), ^{
          [self finishCommandWithResult:result commandId: command];
       });

    } else {
      // Handle older app versions or app not installed case
      
     NSLog(@"IG IS NOT AVAILABLE");
      
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Not installed"];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self finishCommandWithResult:result commandId: command];
     });
    }
}

- (void)shareColorAndStickerImage:(NSString *)backgroundTopColor backgroundBottomColor:(NSString *)backgroundBottomColor stickerImage:(NSData *)stickerImage attributionURL:(NSString *)attributionURL commandId:(NSString *)command  {
    
    // Verify app can open custom URL scheme. If able,
    // assign assets to pasteboard, open scheme.
    NSURL *urlScheme = [NSURL URLWithString:@"instagram-stories://share"];
    if ([[UIApplication sharedApplication] canOpenURL:urlScheme]) {
        
        NSLog(@"IG IS AVAIALBLE");
        
        // Assign background and sticker image assets and
        // attribution link URL to pasteboard
        NSArray *pasteboardItems = @[@{@"com.instagram.sharedSticker.stickerImage" : stickerImage,
                                       @"com.instagram.sharedSticker.backgroundTopColor" : backgroundTopColor,
                                       @"com.instagram.sharedSticker.backgroundBottomColor" : backgroundBottomColor,
                                       @"com.instagram.sharedSticker.contentURL" : attributionURL}];
    
        NSDictionary *pasteboardOptions = @{UIPasteboardOptionExpirationDate : [[NSDate date] dateByAddingTimeInterval:60 * 5]};
        // This call is iOS 10+, can use 'setItems' depending on what versions you support
        [[UIPasteboard generalPasteboard] setItems:pasteboardItems options:pasteboardOptions];
        
        [[UIApplication sharedApplication] openURL:urlScheme options:@{} completionHandler:nil];
        
        NSDictionary *payload = [NSDictionary dictionaryWithObjectsAndKeys:attributionURL, @"url", nil];
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:payload];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [self finishCommandWithResult:result commandId: command];
        });
        
    } else {
        // Handle older app versions or app not installed case
        
        NSLog(@"IG IS NOT AVAILABLE");
        
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Not installed"];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [self finishCommandWithResult:result commandId: command];
        });
    }
}

- (void)finishCommandWithResult:(CDVPluginResult *)result commandId:(NSString *)command {
    NSLog(@"This is callbackurl: %@", command);
    if (command != nil) {
        [self.commandDelegate sendPluginResult:result callbackId:command];
    }
}

@end
