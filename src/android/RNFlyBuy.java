package com.radiusnetworks.plugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import com.radiusnetworks.flybuy.sdk.FlyBuyCore;
import com.radiusnetworks.flybuy.sdk.pickup.PickupManager;
import com.radiusnetworks.flybuy.sdk.data.customer.CustomerInfo;
import com.radiusnetworks.flybuy.sdk.data.room.domain.Customer;
import com.radiusnetworks.flybuy.sdk.data.room.domain.Order;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RNFlyBuy extends CordovaPlugin {
  private static final String TAG = "RadiusNetworksFlyBuy";
  private CallbackContext locationRequestContext = null;
  private boolean isRNFlyBuyConfigured = false;

  static RNFlyBuy app = null;
  static CordovaWebView webView;

  private final static List<String> RNFLYBUY_ACTIONS = Arrays.asList("initialize",
  "onLocationPermissionChanged", "hasLocationPermission", "locationServicesRequestAuthorization", "createCustomer", "updateCustomer", "loginWithToken", "logout", "claimCode", "updateCustomerState", "fetchOrders", "fetchOrderWithRedemptionCode", "getOrdersByStatus");

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView view) {
    Log.d(TAG, "intialize");
    super.initialize(cordova, view);
    app = this;
    webView = view;
  }

  @Override
  protected void pluginInitialize() {
    final String APP_TOKEN = preferences.getString("RN_ANDROID_APP_TOKEN", "default");
    final String ACTION_NAME = "PluginInitialize: ";

    try {

      Log.d(TAG, ACTION_NAME + ": Obtained APP_TOKEN: " + APP_TOKEN);
      FlyBuyCore.configure(cordova.getActivity().getApplicationContext(), APP_TOKEN);
      PickupManager.Companion.getInstance(null).configure(this);
      isRNFlyBuyConfigured = true;
    } catch (Exception e) {
      Log.e(TAG, ACTION_NAME + ": Failed to initialize FlyBuy SDK with exception: " + e.getMessage());
    }
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext ) throws JSONException {
    if (!isRNFlyBuyConfigured) {
      Log.d(TAG, "FlyBuy SDK is not initialized");
      return false;
    }

    if (!RNFLYBUY_ACTIONS.contains(action)) {
      Log.d(TAG, "Invalid action");
      return false;
    }

    try {
      if ("initialize".equals(action)) {
        Log.d(TAG, "FlyBuy SDK is initialized");
      } else if ("onLocationPermissionChanged".equals(action)) {
        onLocationPermissionChanged(args, callbackContext);
      } else if ("hasLocationPermission".equals(action)) {
        hasLocationPermission(callbackContext);
      } else if ("locationServicesRequestAuthorization".equals(action)) {
        locationServicesRequestAuthorization(callbackContext);
      } else if ("createCustomer".equals(action)) {
        createCustomer(args, callbackContext);
      } else if ("updateCustomer".equals(action)) {
        exitIfNoCurrentCustomer("updateCustomer");
        updateCustomer(args, callbackContext);
      } else if ("loginWithToken".equals(action)) {
        loginWithToken(args, callbackContext);
      } else if ("logout".equals(action)) {
        exitIfNoCurrentCustomer("logout");
        logout(callbackContext);
      } else if ("claimCode".equals(action)) {
        exitIfNoCurrentCustomer("claimCode");
        cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            claimCode(args, callbackContext);
          }
        });
      } else if ("updateCustomerState".equals(action)) {
        exitIfNoCurrentCustomer("updateCustomerState");
        updateCustomerState(args, callbackContext);
      } else if ("fetchOrders".equals(action)) {
        exitIfNoCurrentCustomer("fetchOrders");
        cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            fetchOrders(callbackContext);
          }
        });
      } else if ("fetchOrderWithRedemptionCode".equals(action)) {
        cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            fetchOrderWithRedemptionCode(args, callbackContext);
          }
        });
      } else if ("getOrdersByStatus".equals(action)) {
        exitIfNoCurrentCustomer("getOrdersByStatus");
        cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            getOrdersByStatus(args, callbackContext);
          }
        });
       }   
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }

    return true;
  }

   @Override
   public void onStart() {
     super.onStart();
     FlyBuyCore.INSTANCE.onActivityStarted();
   }

   @Override
   public void onStop() {
    super.onStop();
    FlyBuyCore.INSTANCE.onActivityStopped();
   }

   private void onLocationPermissionChanged(JSONArray data, CallbackContext callbackContext) {
    PickupManager.Companion.getInstance(null).onPermissionChanged();
    callbackContext.success();
  }

  private void hasLocationPermission(CallbackContext callbackContext) { 
    final String ACTION_NAME = "hasLocationPermission";

    Boolean isAccessCoarseLocationGranted = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
    Boolean isAccessFineLocationGranted = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

    Boolean isForegroundLocationPermissionGranted = isAccessCoarseLocationGranted && isAccessFineLocationGranted;

    Log.d(TAG, ACTION_NAME + ": " + Boolean.toString(isForegroundLocationPermissionGranted));
    PluginResult result = new PluginResult(PluginResult.Status.OK, isForegroundLocationPermissionGranted);
    callbackContext.sendPluginResult(result);
  }

  private void locationServicesRequestAuthorization(CallbackContext callbackContext) { 
    locationRequestContext = callbackContext;
    PermissionHelper.requestPermission(this, 100, Manifest.permission.ACCESS_FINE_LOCATION );
  }

  @Override  /* PermissionHelper.requestPermission callback */
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {

    String[] permissionName = permissions[0].toString().split("\\.");
    Boolean grantResult = grantResults[0] == PackageManager.PERMISSION_GRANTED;
    Log.d(TAG, permissionName + ": " + Boolean.toString(grantResult));

    PluginResult result = new PluginResult(PluginResult.Status.OK, grantResult);
    result.setKeepCallback(true);
    locationRequestContext.sendPluginResult(result);
    ((PickupManager)PickupManager.manager.getInstance(null)).onLocationPermissionChanged();
    //PickupManager.getInstance().onLocationPermissionChanged();
  }

  private void createCustomer(JSONArray data, CallbackContext callbackContext) {
    final String ACTION_NAME = "createCustomer";
    final CustomerInfo currentCustomer = getCurrentCustomerInfo();

    if (currentCustomer != null) {
      updateCustomer(data, callbackContext);
      return;
    }

    try {
      JSONObject rawCustomerInfo = data.getJSONObject(0);
      Boolean termsOfService = rawCustomerInfo.optBoolean("termsOfService", false);
      Boolean ageVerification = rawCustomerInfo.optBoolean("ageVerification", false);
      CustomerInfo customerInfo = getCustomerInfo(rawCustomerInfo);

      FlyBuyCore.customer.create(customerInfo, termsOfService, ageVerification, null, null (customer, sdkError) -> {
        PluginResult result = null;

        if (sdkError != null) {
          result = PluginHelper.handleSdkError(sdkError, ACTION_NAME);
        } else if (customer != null) {
          JSONObject user = PluginHelper.toJsonObject(customer, null);
          result = new PluginResult(PluginResult.Status.OK, user);
        }
        
        callbackContext.sendPluginResult(result);
        return null;
      });
    } catch(JSONException e) {
      PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
    }
  }  

  private void updateCustomer(JSONArray data, CallbackContext callbackContext) {
    final String ACTION_NAME = "updateCustomer";

    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject rawCustomerInfo = data.getJSONObject(0);
          CustomerInfo customerInfo = getCustomerInfo(rawCustomerInfo);
    
          FlyBuyCore.customer.update(customerInfo, (customer, sdkError) -> {
            PluginResult result = null;
    
            if (sdkError != null) {
              result = PluginHelper.handleSdkError(sdkError, ACTION_NAME);
            } else if (customer != null) {
              JSONObject user = PluginHelper.toJsonObject(customer, null);
              result = new PluginResult(PluginResult.Status.OK, user);
            }
            
            callbackContext.sendPluginResult(result);
            return null;
          });
        } catch(JSONException e) {
          PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
        }
      }
    });
  }
  
  private void loginWithToken(JSONArray data, CallbackContext callbackContext) {
    final String ACTION_NAME = "loginWithToken";
    
    try {
      final String TOKEN = data.getString(0); 

      if (isEmptyString(TOKEN)) {
        PluginHelper.sendError(callbackContext, "Token cannot be null or an empty string.");
        return;
      }

      FlyBuyCore.customer.loginWithToken(TOKEN, (customer, sdkError) -> {
        PluginResult result = null;

        if (sdkError != null) {
          result = PluginHelper.handleSdkError(sdkError, ACTION_NAME);
        } else if (customer != null) {
          result = new PluginResult(PluginResult.Status.OK);
        }
        
        callbackContext.sendPluginResult(result);
        return null;
      });
    } catch (JSONException e) {
      PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
    }
  }

  private void claimCode(JSONArray data, CallbackContext callbackContext) {
    final String ACTION_NAME = "claimCode";
    CustomerInfo customerInfo = getCurrentCustomerInfo();

    try {
      JSONObject raw = data.getJSONObject(0);
      final String REDEEM_CODE = raw.optString("redemptionCode"); 
      String pickupType = getPickupTypeString(raw.optString("pickupType"));

      if (isEmptyString(REDEEM_CODE)) {
        PluginHelper.sendError(callbackContext, "Redemption code cannot be null or an empty string.");
        return;
      }

      FlyBuyCore.orders.claim(REDEEM_CODE, customerInfo, pickupType, (order, sdkError) -> {
        PluginResult result = null;
        if (sdkError != null) {
          result = PluginHelper.handleSdkError(sdkError, ACTION_NAME);
        } else if (order != null) {
          result = PluginHelper.getOrderResult(order);
        }
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
        return null;
      });

    } catch (JSONException e) {
      PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
    }
  }

  private void updateCustomerState(JSONArray data, CallbackContext callbackContext) { 
    final String ACTION_NAME = "updateCustomerState";

    try { 
      JSONObject raw = data.getJSONObject(0);
      final int ORDER_ID = raw.optInt("orderId", 0);
      String customerState = raw.optString("customerState");

      if (ORDER_ID < 1) {
        PluginHelper.sendError(callbackContext, "orderId must be a number and greater than 0");
        return;
      }
      
      FlyBuyCore.orders.updateCustomerState(ORDER_ID, customerState, (order, sdkError) -> {
        PluginResult result = PluginHelper.handleOrderResult(order, sdkError, ACTION_NAME);
        callbackContext.sendPluginResult(result);
        return null;
      });
    } catch (JSONException e) {
      PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
    }
  }

  private void fetchOrders(CallbackContext callbackContext) { 
    final String ACTION_NAME = "fetchOrders";
    FlyBuyCore.orders.fetch((orders, sdkError) -> {
      PluginResult result = null;
      if (sdkError != null) {
        result = PluginHelper.handleSdkError(sdkError, ACTION_NAME);
      } else if (orders != null) {
        result = PluginHelper.getOrdersResult(orders);
      }
      callbackContext.sendPluginResult(result);
      return null;
    });
  }

  private void fetchOrderWithRedemptionCode(JSONArray data, CallbackContext callbackContext) { 
    final String ACTION_NAME = "fetchOrderWithRedemptionCode";
   
    try {
      final String REDEEM_CODE = data.getString(0); 

      if (isEmptyString(REDEEM_CODE)) {
        PluginHelper.sendError(callbackContext, "Redemption code cannot be null or an empty string.");
        return;
      }

      FlyBuyCore.orders.fetch(REDEEM_CODE, (order, sdkError) -> {
        PluginResult result = PluginHelper.handleOrderResult(order, sdkError, ACTION_NAME);
        callbackContext.sendPluginResult(result);
        return null;
      });

    } catch (JSONException e) {
      PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
    }
  }

  private void getOrdersByStatus(JSONArray data, CallbackContext callbackContext) { 
    final String ACTION_NAME = "getOrdersByStatus";
   
    try {
 
      final String status = data.getString(0); 
      List<Order> orders = null;

      if (status.equals("open")) {
        orders = FlyBuyCore.orders.getOpen();
      } else if (status.equals("closed")) {
        orders = new ArrayList<Order>(); // not supported
      } else {
        orders = FlyBuyCore.orders.getAll();
      }
      PluginResult result = PluginHelper.getOrdersResult(orders);
      callbackContext.sendPluginResult(result);
    } catch (JSONException e) {
      PluginHelper.sendJSONExceptionError(callbackContext, e, ACTION_NAME);
    } 
  }

  private void logout(CallbackContext callbackContext) {
    FlyBuyCore.customer.logout((sdkError) -> {
      PluginResult result = sdkError != null? PluginHelper.handleSdkError(sdkError, "logout") : new PluginResult(PluginResult.Status.OK);
      callbackContext.sendPluginResult(result);
      return null;
    });
  }

  // Internal functions
  private CustomerInfo getCurrentCustomerInfo() {
    Customer currentCustomer = FlyBuyCore.customer.getCurrent();

    if (currentCustomer == null) { return null; }
    return new CustomerInfo(currentCustomer.getName(), currentCustomer.getPhone(), currentCustomer.getCarType(), currentCustomer.getCarColor(), currentCustomer.getLicensePlate());
  }

  private CustomerInfo getCustomerInfo(JSONObject raw) {
    String name = raw.optString("name");
    String carType = raw.optString("carType");
    String carColor = raw.optString("carColor");
    String licensePlate = raw.optString("licensePlate");
    String phone =  raw.optString("phone"); 
    return new CustomerInfo(name, phone, carType, carColor, licensePlate);
  }

  private enum PickupType {
    CURBSIDE,
    PICKUP,
    DELIVERY
  }
  
  private String getPickupTypeString(String orderType) {
    if (orderType.equalsIgnoreCase(PickupType.CURBSIDE.toString())) {
      return PickupType.CURBSIDE.toString().toLowerCase();
    } else if (orderType.equalsIgnoreCase(PickupType.PICKUP.toString())) {
      return PickupType.PICKUP.toString().toLowerCase();
    } else if (orderType.equalsIgnoreCase(PickupType.DELIVERY.toString())) {
      return PickupType.DELIVERY.toString().toLowerCase();
    }
    return "";
  }

  private void exitIfNoCurrentCustomer(String ACTION_NAME) throws Exception {
    if (getCurrentCustomerInfo() == null) {
      final String MSG = "Customer not authenticated.";
      Log.e(TAG, ACTION_NAME + " failed: " +  MSG);
      throw new Exception(MSG);
    }
  }

  private Boolean isEmptyString(String value) {
    return (value == null || value.trim().isEmpty());
  }

 }
