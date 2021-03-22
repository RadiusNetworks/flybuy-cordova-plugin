# cordova-plugin-radius-networks

A Cordova Plugin for Radius Networks FlyBuy SDK.

## Supported Platforms

- Android
- iOS

## Requirements

- A Deeplinks/Universal links plugin eg:
https://github.com/ionic-team/ionic-plugin-deeplinks

- Android

  - The FlyBuy Android SDK is written in Kotlin and using AndroidX libraries. Therefore, this plugin requires AndroidX libraries and Kotlin enabled. It requires the project compileSdkVersion to be 29 and above.

    The default values set for Kotlins as follows:

    ```xml
      <preference name="GradlePluginKotlinEnabled" value="true" />
      <preference name="GradlePluginKotlinCodeStyle" value="official" />
      <preference name="GradlePluginKotlinVersion" value="1.4.10" />
    ```
    Note: The setting above will be added to your `config.xml` by the plugin, no manual change required.

- iOS
  - Add the following code to the App `config.xml` file:
      ```xml
      <platform name="ios">
        ...
        <config-file target="*-Info.plist" parent="NSLocationAlwaysAndWhenInUseUsageDescription" >
          <string>To accurately locate you for order pickup.</string>
        </config-file>

        <config-file target="*-Info.plist" parent="NSLocationWhenInUseUsageDescription" >
          <string>To accurately locate you for order pickup.</string>
        </config-file>
      </platform>
      ```
      Note: the setting above will be used to ask for Location Services permissions, feel free to update it according to your needs.

## Installation

1. Add the plugin to your Cordova project:

`cordova plugin add cordova-plugin-radius-networks --variable RN_IOS_APP_TOKEN=your_radius_token --variable RN_ANDROID_APP_TOKEN=your_radius_token --variable RN_GOOGLE_ANDROID_API_KEY=your_google_api_key`

## Notes

The plugin will need to request the proper permissions in order to use the SDK:

- iOS:
  - It will enable the following "Background Modes": `Location updates` and `Background fetch`.

- Android
  - It will set the `FINE_LOCATION` permission.

## Methods

Please use the `www/FlyBuy.js` for more details, these are the current available methods:

- Orders
  - fetchOrders
    - `window.FlyBuy.fetchOrders(onFetch, onFetchError);`
  - fetchOrderWithRedemptionCode
    - `window.FlyBuy.fetchOrderWithRedemptionCode(onRedeem, onRedeemError, code);`
  - claimCode
    - `window.FlyBuy.claimCode(onClaimSuccess, onClaimError, {redemptionCode: code});`
  - getOrdersByStatus
    - `window.FlyBuy.getOrdersByStatus(onGetOrdersByStatusSuccess, onGetOrdersByStatusError, 'open');`

- Customer
  - createCustomer:
    - `window.FlyBuy.createCustomer(onCreateCustomerSuccess, onCreateCustomerError, customerInfo);`
  - updateCustomer
    - `window.FlyBuy.updateCustomer(onUpdateCustomerSuccess, onUpdateCustomerError, customerInfo);`
  - loginWithToken
    - `window.FlyBuy.loginWithToken(onLoginWithTokenSuccess, onLoginWithTokenError, RadiusCustomerToken);`
  - logout
    - `window.FlyBuy.logout(onLogoutSuccess, onLogoutError);`

- Location Services
  - locationServicesRequestAuthorization
    - `window.FlyBuy.locationServicesRequestAuthorization(onLocationServicesRequestAuthorizationSuccess, onLocationServicesRequestAuthorizationError);`
  - hasLocationPermission
    - `window.FlyBuy.hasLocationPermission(onHasLocationPermissionSuccess, onHasLocationPermissionError);`
  - onLocationPermissionChanged
    - `window.FlyBuy.onLocationPermissionChanged(onLocationPermissionChangedSuccess, onLocationPermissionChangedError);`

