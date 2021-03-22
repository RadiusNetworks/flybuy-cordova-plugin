#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import <CoreLocation/CoreLocation.h>

@interface RNFlyBuy : CDVPlugin <CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager* locationManager;
@property (nonatomic, strong) NSDictionary *customerStatesMapping;
@property (nonatomic, strong) NSArray *customerStatesMappingArray;

- (void)initialize:(CDVInvokedUrlCommand*)command;

- (void)claimCode:(CDVInvokedUrlCommand*)command;

- (void)loginWithToken:(CDVInvokedUrlCommand*)command;

- (void)createCustomer:(CDVInvokedUrlCommand*)command;

- (void)updateCustomer:(CDVInvokedUrlCommand*)command;

- (void)fetchOrderWithRedemptionCode:(CDVInvokedUrlCommand*)command;

- (void)fetchOrders:(CDVInvokedUrlCommand*)command;

- (void)hasLocationPermission:(CDVInvokedUrlCommand*)command;

- (void)locationServicesRequestAuthorization:(CDVInvokedUrlCommand*)command;

- (void)updateCustomerState:(CDVInvokedUrlCommand*)command;

- (void)getOrdersByStatus:(CDVInvokedUrlCommand*)command;

- (void)logout:(CDVInvokedUrlCommand*)command;

@end
