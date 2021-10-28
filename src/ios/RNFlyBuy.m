#import <Cordova/CDV.h>
#import <Cordova/CDVPluginResult.h>
#import <FlyBuy/FlyBuy-Swift.h>
#import <FlyBuyPickup/FlyBuyPickup-Swift.h>
#import "RNFlyBuy.h"


@implementation RNFlyBuy

#pragma mark - initialization

- (void)pluginInitialize
{
  self.locationManager = [[CLLocationManager alloc] init];
  self.locationManager.delegate = self;

  // Maps CustomerState ENUM in order to have the same representation in iOS/Android
  self.customerStatesMapping = @{
    @"CREATED":@(CustomerStateCreated),
    @"EN_ROUTE":@(CustomerStateEnRoute),
    @"NEARBY":@(CustomerStateNearby),
    @"ARRIVED":@(CustomerStateArrived),
    @"WAITING":@(CustomerStateWaiting),
    @"COMPLETED":@(CustomerStateCompleted)
  };
  self.customerStatesMappingArray = @[@"CREATED", @"EN_ROUTE", @"NEARBY", @"ARRIVED", @"WAITING", @"COMPLETED"];

  __weak RNFlyBuy* weakSelf = self;
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(finishedLaunching:) name:UIApplicationDidFinishLaunchingNotification object:nil];
}

- (void)finishedLaunching:(NSNotification *)notification
{
  NSString *applicationToken = [self.commandDelegate.settings objectForKey:@"rn_ios_app_token"];

  if (applicationToken != nil && ![applicationToken isEqualToString:@"undefined"]) {
    [FlyBuy configure: @{@"token" : applicationToken} ];
  } else {
    NSLog(@"Missing FlyBuy Application Token!");
  }
}

#pragma mark - Fly Buy

- (void)createCustomer:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];

  if (![self check:command hasArgs:args]) {
    return;
  }
  
  BOOL termsOfService = [args objectForKey:@"termsOfService"];
  BOOL ageVerification = [args objectForKey:@"ageVerification"];
  NSString *name = [args objectForKey:@"name"];
  NSString *carType = [args objectForKey:@"carType"];
  NSString *carColor = [args objectForKey:@"carColor"];
  NSString *licensePlate = [args objectForKey:@"licensePlate"];
  NSString *phone = [args objectForKey:@"phone"];
  
  __weak RNFlyBuy* weakSelf = self;

  [self.commandDelegate runInBackground:^{
    Customer *flybuyCustomer = [[FlyBuy customer] current];
    
    if (flybuyCustomer == nil) {
      CustomerInfo *info = [[CustomerInfo alloc] initWithName:name carType:carType carColor:carColor licensePlate:licensePlate phone:phone];

      [[FlyBuy customer] create:info termsOfService:termsOfService ageVerification:ageVerification callback:^(Customer * _Nullable customer, NSError * _Nullable error) {
        if (customer) {
          sendCustomerResult(command, customer, weakSelf);
        } else {
          [weakSelf sendError:command withMessage:[NSString stringWithFormat:@"ERROR: Could not create Customer: %@!", error.localizedDescription]];
        }
      }];
    } else {
      [self updateCustomer:command];
    }
  }];
}

static void sendCustomerResult(CDVInvokedUrlCommand *command, Customer * _Nullable customer, RNFlyBuy *__weak weakSelf) {
  [weakSelf.commandDelegate sendPluginResult:[CDVPluginResult
                                              resultWithStatus:(CDVCommandStatus)CDVCommandStatus_OK
                                              messageAsDictionary:@{@"apiToken": customer.token}]
                                  callbackId:command.callbackId];
}

- (void)updateCustomer:(CDVInvokedUrlCommand*)command {
    NSDictionary *args = [command.arguments objectAtIndex:0];
    
    if (![self check:command hasArgs:args]) {
      return;
    }
  
    NSString *name = [args objectForKey:@"name"];
    NSString *carType = [args objectForKey:@"carType"];
    NSString *carColor = [args objectForKey:@"carColor"];
    NSString *licensePlate = [args objectForKey:@"licensePlate"];
    NSString *phone = [args objectForKey:@"phone"];

  __weak RNFlyBuy* weakSelf = self;

  [self.commandDelegate runInBackground:^{
    if ([[FlyBuy customer] current] == nil) {
      [weakSelf sendError:command withMessage:@"ERROR: Customer not found!"];
      return;
    }

    CustomerInfo *info = [[CustomerInfo alloc] initWithName:name carType:carType carColor:carColor licensePlate:licensePlate phone:phone];

    [[FlyBuy customer] update:info callback:^(Customer * _Nullable customer, NSError * _Nullable error) {
      if (customer) {
        sendCustomerResult(command, customer, weakSelf);
      } else {
        [weakSelf sendError:command withMessage:[NSString stringWithFormat:@"ERROR: %@!", error.localizedDescription]];
      }
    }];
  }];
}

- (void)claimCode:(CDVInvokedUrlCommand*)command {
  [self.commandDelegate runInBackground:^{
    NSDictionary *args = [command.arguments objectAtIndex:0];

    if (![self check:command hasArgs:args]) {
      return;
    }
    NSString *redemptionCode = nil;
    
    if([args objectForKey:@"redemptionCode"] != nil){
      redemptionCode = [args objectForKey:@"redemptionCode"];
    }

    NSString *pickupType = [args objectForKey:@"pickupType"];
    
    if (redemptionCode == nil || redemptionCode == (NSString *)[NSNull null]) {
      [self sendError:command withMessage:@"Missing args."];
      return;
    }

    Customer *flybuyCustomer = [[FlyBuy customer] current];

    __weak RNFlyBuy* weakSelf = self;
    
    if (flybuyCustomer == nil) {
      [weakSelf sendError:command withMessage:@"ERROR: Customer not found!"];
      return;
    }

    if (pickupType == nil) {
      [[FlyBuy orders] claimWithRedemptionCode:redemptionCode customerInfo:flybuyCustomer.info callback:^(Order * _Nullable order, NSError * _Nullable error) {
        handleClaim(command, error, weakSelf);
      }];
    } else {
      [[FlyBuy orders] claimWithRedemptionCode:redemptionCode customerInfo:flybuyCustomer.info pickupType:pickupType callback:^(Order * _Nullable order, NSError * _Nullable error) {
        handleClaim(command, error, weakSelf);
      }];
    }
  }];
}

static void handleClaim(CDVInvokedUrlCommand *command, NSError * _Nullable error, RNFlyBuy *__weak weakSelf) {
  if (!error) {
    [weakSelf sendSuccess:command withMessage:@"Order Claimed"];
  } else {
    [weakSelf sendError:command withMessage:[NSString stringWithFormat:@"ERROR: %@!", error.localizedDescription]];
  }
}

- (void)fetchOrderWithRedemptionCode:(CDVInvokedUrlCommand*)command {
  [self.commandDelegate runInBackground:^{
    NSString *redemptionCode = [command.arguments objectAtIndex:0];
    
    if (redemptionCode == nil || redemptionCode == (NSString *)[NSNull null]) {
      [self sendError:command withMessage:@"Missing args."];
      return;
    }

    __weak RNFlyBuy* weakSelf = self;

    [[FlyBuy orders] fetchWithRedemptionCode:redemptionCode callback:^(Order * _Nullable order, NSError * _Nullable error) {
      CDVPluginResult *pluginResult = nil;
      if (order) {
        pluginResult = [CDVPluginResult resultWithStatus:(CDVCommandStatus)CDVCommandStatus_OK messageAsDictionary:[weakSelf dictionaryFromOrder:order]];
        NSLog(@"DATA: %@", [weakSelf dictionaryFromOrder:order]);
      } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"This order was not found. The order may have been redeeded already or the redemption code was incorrect"];
      }
      NSLog(@"Order: %@ - Error: %@", order, error);
      [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

  }];
}

- (void)fetchOrders:(CDVInvokedUrlCommand*)command {
  
  if (![[FlyBuy customer] current]) {
    [self sendError:command withMessage:@"ERROR: Customer not found!"];
    return;
  }

  __weak RNFlyBuy* weakSelf = self;

  [self.commandDelegate runInBackground:^{
    [[FlyBuy orders] fetchWithCallback:^(NSArray<Order *> *orders, NSError *error) {

      if (error) {
        [weakSelf sendError:command withMessage:[NSString stringWithFormat:@"ERROR: fetching orders: %@!", error.localizedDescription]];
      }

      [weakSelf.commandDelegate sendPluginResult:[weakSelf pluginResultFrom:orders] callbackId:command.callbackId];
    }];
  }];
}

- (void)loginWithToken:(CDVInvokedUrlCommand*)command {
  [self.commandDelegate runInBackground:^{
    NSString *token = [command.arguments objectAtIndex:0];

    if (token == nil || token == (NSString *)[NSNull null]) {
      [self sendError:command withMessage:@"ERROR: Invalid token!"];
      return;
    }

    __weak RNFlyBuy* weakSelf = self;

    [[FlyBuy customer] loginWithTokenWithToken:token callback:^(Customer * _Nullable customer, NSError * _Nullable error) {
      if (customer) {
        [weakSelf sendSuccess:command withMessage:@"Costumer logged!"];
      } else {
        [weakSelf sendError:command withMessage:[NSString stringWithFormat:@"ERROR: login failed: %@!", error.localizedDescription]];
      }
    }];
  }];
}

- (void)logout:(CDVInvokedUrlCommand*)command {
  [[FlyBuy customer] logout];
  [self sendSuccess:command withMessage:@"Logout complete!"];
}

- (void)updateCustomerState:(CDVInvokedUrlCommand*)command {

  NSDictionary *args = [command.arguments objectAtIndex:0];

  if (![self check:command hasArgs:args]) {
    return;
  }

  NSInteger orderID = [[args objectForKey:@"orderId"] integerValue];
  NSString *customerState = [args objectForKey:@"customerState"];

  if (![self.customerStatesMapping objectForKey:customerState]) {
    [self sendError:command withMessage:@"ERROR: invalid customer state"];
    return;
  }

  __weak RNFlyBuy* weakSelf = self;

  [[FlyBuy orders] eventWithOrderID:orderID customerState:[self customerStateFrom:customerState] callback:^(Order * _Nullable order, NSError * _Nullable error) {
    NSLog(@"Order: %@ - Error: %@", order, error);
    if (error) {
      [weakSelf sendError:command withMessage:[NSString stringWithFormat:@"ERROR: Could not set Costumer state! %@!", error.localizedDescription]];
    } else {
      [weakSelf sendSuccess:command withMessage:[NSString stringWithFormat:@"Costumer state set to: %@", customerState]];
    }
  }];
}

// Maps js CustomerState state to Objective-C Enum
- (CustomerState)customerStateFrom:(NSString *)customerState {
  switch ([[self.customerStatesMapping valueForKey:customerState] integerValue]) {
    case CustomerStateCreated:
      return CustomerStateCreated;
    case CustomerStateEnRoute:
      return CustomerStateEnRoute;
    case CustomerStateNearby:
      return CustomerStateNearby;
    case CustomerStateArrived:
      return CustomerStateArrived;
    case CustomerStateWaiting:
      return CustomerStateWaiting;
    case CustomerStateCompleted:
      return CustomerStateCompleted;
    default:
      return CustomerStateCreated;
  }
}

- (void)getOrdersByStatus:(CDVInvokedUrlCommand*)command {
  
  if (![[FlyBuy customer] current]) {
    [self sendError:command withMessage:@"ERROR: Customer not found!"];
    return;
  }
  
  NSString *orderStatus = [command.arguments objectAtIndex:0];
  
  if (orderStatus == nil || orderStatus == (NSString *)[NSNull null]) {
    [self sendError:command withMessage:@"ERROR: Invalid Order Status!"];
    return;
  }
  
  NSArray *orders = nil;

  if ([orderStatus  isEqual: @"all"]) {
    orders = [FlyBuy orders].all;
  }

  if ([orderStatus  isEqual: @"open"]) {
    orders = [FlyBuy orders].open;
  }
  
  if ([orderStatus  isEqual: @"closed"]) {
    orders = [FlyBuy orders].closed;
  }
  
  [self.commandDelegate sendPluginResult:[self pluginResultFrom:orders] callbackId:command.callbackId];
}

#pragma mark - Location Services

- (void)hasLocationPermission:(CDVInvokedUrlCommand*)command {
  BOOL locationPermissionEnabled = NO;

  CLAuthorizationStatus authorizationStatus = [CLLocationManager authorizationStatus];

  if ((authorizationStatus == kCLAuthorizationStatusAuthorizedAlways) || (authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse)) {
    locationPermissionEnabled = YES;
  }

  [self.commandDelegate
    sendPluginResult:[CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsBool:locationPermissionEnabled]
   callbackId:command.callbackId];
}

- (void)locationServicesRequestAuthorization:(CDVInvokedUrlCommand*)command {

  [self.locationManager requestWhenInUseAuthorization];

  [self.commandDelegate
    sendPluginResult:[CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsString:@"Requested Location Authorization!"]
   callbackId:command.callbackId];
}

#pragma mark - Utility

- (BOOL)check:(CDVInvokedUrlCommand *)command hasArgs:(NSDictionary *)args {
  if (args == nil || args == (NSDictionary *)[NSNull null] || ![args isKindOfClass:[NSDictionary class]]) {
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"ERROR: Missing arguments!"] callbackId:command.callbackId];
    return NO;
  }

  return YES;
}

static NSString * _Nonnull convertDate(NSDate *date) {
  if (date == nil) {
    return @"";
  }

  NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
  [dateFormat setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZZZZZ"];

  return [dateFormat stringFromDate:date];
}

- (NSString *) customerStatefromInteger:(NSNumber *)state {
  for (NSString *key in self.customerStatesMapping) {
    if ([self.customerStatesMapping valueForKey:key] == state) {
      return key;
    }
  }

  return nil;
}

- (NSDictionary *)dictionaryFromOrder:(Order*)order {

  NSMutableDictionary *dict =  [NSMutableDictionary dictionaryWithCapacity:1];

  [dict setValue:[NSNumber numberWithInteger:order.id] forKey:@"id"];
  [dict setValue:order.partnerIdentifier forKey:@"partnerIdentifier"];

  [dict setValue:convertDate(order.createdAt) forKey:@"createdAt"];
  [dict setValue:[NSNull null] forKey:@"etaAt"];
  if (order.etaAt != nil) {
    [dict setValue:convertDate(order.etaAt) forKey:@"etaAt"];
  }
  [dict setValue:order.pickupType forKey:@"pickupType"];
  [dict setValue:[NSNumber numberWithInteger:order.state] forKey:@"orderState"];
  [dict setValue:[self.customerStatesMappingArray objectAtIndex:order.customerState] forKey:@"customerState"];
  [dict setValue:[NSNull null] forKey:@"pickupWindow"];
  if (order.pickupWindow != nil) {
    [dict setObject:@{
      @"start":convertDate(order.pickupWindow.start),
      @"end":convertDate(order.pickupWindow.end)
    } forKey:@"pickupWindow"];
  }
  [dict setValue:order.redemptionCode forKey:@"redemptionCode"];

  [dict setObject:@{
    @"id":[NSNumber numberWithInteger:order.siteID],
    @"name":order.siteName,
    @"partnerIdentifier":order.sitePartnerIdentifier,
    @"fullAddress":order.siteFullAddress,
    @"phone":order.sitePhone,
  } forKey:@"site"];

  [dict setObject:@{
    @"name": (order.customerName) ? order.customerName : @"",
    @"carColor": (order.customerCarColor) ? order.customerCarColor : @"",
    @"carType": (order.customerCarType) ? order.customerCarType : @"",
    @"licensePlate": (order.customerLicensePlate) ? order.customerLicensePlate : @"",
  } forKey:@"customer"];

  return dict;
}

- (void)sendError:(CDVInvokedUrlCommand *)command withMessage:(NSString *)message {
  [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message] callbackId:command.callbackId];
}

- (void)sendSuccess:(CDVInvokedUrlCommand *)command withMessage:(NSString *)message {
  [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message] callbackId:command.callbackId];
}

- (NSMutableArray *)mapOrders:(NSArray<Order *> *)orders {
  NSMutableArray *parsedOrders = [NSMutableArray arrayWithCapacity:[orders count]];

  for (Order *order in orders) {
    NSDictionary *entryDict = [self dictionaryFromOrder:order];
    [parsedOrders addObject:entryDict];
  }

  return parsedOrders;
}

- (CDVPluginResult *)pluginResultFrom:(NSArray<Order *> *)orders {
  return [CDVPluginResult resultWithStatus:(CDVCommandStatus)CDVCommandStatus_OK messageAsArray:[self mapOrders:orders]];
}

@end
