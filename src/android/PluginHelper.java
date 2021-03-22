
package com.radiusnetworks.plugin;

import com.radiusnetworks.flybuy.sdk.data.room.domain.Order;
import com.radiusnetworks.flybuy.sdk.data.common.SdkError;

import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;

public final class PluginHelper { 
  private static final String TAG = "PluginHelper";

  private PluginHelper() { }

  public static PluginResult handleSdkError(SdkError sdkError, String actionName)  {
    String message = sdkError.userError();
    Log.d(TAG, actionName + " Fail: " + message);
    return new PluginResult(PluginResult.Status.ERROR, message);
  }

  public static void sendJSONExceptionError(CallbackContext callbackContext, JSONException e, String actionName) {
    Log.e(TAG, actionName + " Fail: " + e.getMessage());
    e.printStackTrace();
    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
  }

  public static void sendError(CallbackContext callbackContext, String message) {
    PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
    callbackContext.sendPluginResult(result);
  }

  public static PluginResult handleOrderResult(Order order, SdkError sdkError, String actionName) {
    PluginResult result = null;
    if (sdkError != null) {
      result = handleSdkError(sdkError, actionName);
    } else if (order != null) {
      result = getOrderResult(order);
    }
    return result;
  }

  public static PluginResult getOrderResult(Order order) {
    JSONObject orderJSONObject = orderInJsonObject(order);
    return new PluginResult(PluginResult.Status.OK, orderJSONObject);
  }

  public static PluginResult getOrdersResult(List<Order> orders) {
    JSONArray allOrders = ordersInJsonArray(orders);
    return new PluginResult(PluginResult.Status.OK, allOrders);
  }

  public static JSONArray ordersInJsonArray(List<Order> orders) {
    JSONArray allOrders = new JSONArray();
    for (Order order: orders) {
      JSONObject o = orderInJsonObject(order);
      allOrders.put(o);
    }
    return allOrders;
  }

  public static JSONObject orderInJsonObject(Order order) {
    List<String> excludedAttributes = Arrays.asList("customer", "pickupWindow",
    "site", "type", "displayDetail", "displayName"); 
    JSONObject resultOrder = toJsonObject(order, excludedAttributes);

    try {
      resultOrder.put("customer", order.getCustomer() == null ? JSONObject.NULL : toJsonObject(order.getCustomer(), null));
      resultOrder.put("pickupWindow", order.getPickupWindow() == null ? JSONObject.NULL : toJsonObject(order.getPickupWindow(), null));
      resultOrder.put("site",  toJsonObject(order.getSite(), null));
      return resultOrder;
    } catch ( JSONException e)  {
      Log.e(TAG, "orderInJsonObject Fail: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  public static JSONObject toJsonObject(Object target, List excludedAttributes) {
    JSONObject destination = new JSONObject();

    try {
      for (Field f: target.getClass().getDeclaredFields()) {
        f.setAccessible(true);
        String key = f.getName();
        if (excludedAttributes == null || (excludedAttributes != null && !excludedAttributes.contains(key))) {
          //Log.d(TAG, "toJsonObject key " + key + ", " + f.get(target));
          destination.put(key, f.get(target) == null ? JSONObject.NULL : f.get(target));
        }
      }
      return destination;
    } catch ( IllegalAccessException | JSONException e) {
      Log.e(TAG, "toJsonObject Fail: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

}