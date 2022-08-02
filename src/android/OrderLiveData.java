package com.radiusnetworks.plugin;

import com.radiusnetworks.plugin.RNFlyBuy;
import com.radiusnetworks.flybuy.sdk.FlyBuy;
import com.radiusnetworks.flybuy.sdk.data.room.domain.Order;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import java.util.List;
import java.util.Arrays;
import java.lang.reflect.Field;

public class OrderLiveData {
  private static final String TAG = "OrderLiveData";
  private int subscribedOrderId = 0;
  private LiveData<Order> liveOrderData;
  static RNFlyBuy appInstance = null;
  private Observer<Order> orderObserver = new Observer<Order>() {
    @Override
    public void onChanged(Order order) {
      if (order != null) {
        appInstance.webView.loadUrl("javascript:cordova.fireWindowEvent('ordersUpdated')");
      }
    }
  };

  public OrderLiveData (RNFlyBuy instance) {
    appInstance = instance;
  }

  private LiveData<Order> getLiveData() {
    if (liveOrderData == null && subscribedOrderId > 0) {
      liveOrderData = FlyBuyCore.orders.getOrder(subscribedOrderId);
    }
    return liveOrderData;
  }

  private void subscribe(int orderId) {
    subscribedOrderId = orderId;
    if (getLiveData() != null) {
      getLiveData().observeForever(orderObserver);
      Log.d(TAG, "Subscribed to Order #" + orderId);
    }
  }

  public void unsubscribe() {
    if (getLiveData() != null) {
      Log.d(TAG, "Unsubscribed to Order #" + subscribedOrderId);
      getLiveData().removeObserver(orderObserver);
      liveOrderData = null;
    }
    subscribedOrderId = 0;
  }

  public void subscribeTo(int orderId) {
    if (subscribedOrderId == 0 && subscribedOrderId != orderId) {
      subscribe(orderId);
    } else if (subscribedOrderId != orderId) {
      unsubscribe();
      subscribe(orderId);
    }
  }
}
