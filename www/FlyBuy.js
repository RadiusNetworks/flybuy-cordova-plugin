"use strict";
var exec = require("cordova/exec");

function FlyBuy() {
  this.eventFired = null;
}

FlyBuy.prototype.initialize = function (success, error, args) {
  exec(success, error, "FlyBuy", "initialize", [args]);
};

/* Android Only Starts */
/**
 * @function onLocationPermissionChanged
 * Notify FlyBuy SDK that location permission has changed.
 * @param {function()} success - callback, always return success
 * @param {function(string)} error - callback, will not be called
 *
 */
FlyBuy.prototype.onLocationPermissionChanged = function (success, error, args) {
  exec(success, error, "FlyBuy", "onLocationPermissionChanged", [args]);
};
/* Android only Ends */

/**
 * @function hasLocationPermission
 * Return  ACCESS_FINE_LOCATION location service
 * @param {function(string)} success - callback, existing ACCESS_FINE_LOCATION Permission boolean string as callback param
 * @param {function()} error - callback, will not be called
 */
FlyBuy.prototype.hasLocationPermission = function (success, error) {
  exec(success, error, "FlyBuy", "hasLocationPermission");
};

/**
 * @function locationServicesRequestAuthorization
 * Request permissions for ACCESS_FINE_LOCATION
 * @param {function(string)} success - callback, existing ACCESS_FINE_LOCATION Permission  boolean string as callback param
 * @param {function()} error - callback, will not be called
 *
 * More details: https://developer.android.com/training/location/permissions#foreground
 */
FlyBuy.prototype.locationServicesRequestAuthorization = function (success, error) {
  exec(success, error, "FlyBuy", "locationServicesRequestAuthorization");
};

/**
 * @function createCustomer
 * Create customer within FlyBuy SDK
 * @param {function(string)} success - callback, customer.apiToken as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {object} args
 * @param {string} args.name - user's name
 * @param {string} args.carType - user's car type
 * @param {string} args.carColor - user's car color
 * @param {string} args.licensePlate - user's license plate
 * @param {string} args.phone - optional. user's phone number, format: "202-401-9200"
 * @param {boolean} args.termsOfService - user's consent for terms of service
 * @param {boolean} args.ageVerification - user's consent for age of verification
 */
FlyBuy.prototype.createCustomer = function (success, error, args) {
  exec(success, error, "FlyBuy", "createCustomer", [args]);
};

/**
 * @function updateCustomer
 * Update customer within FlyBuy SDK
 * @param {function(object)} success - callback, customer object {@link Customer} as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {object} args
 * @param {string} args.name - required. user's name
 * @param {string} args.carType - user's car type
 * @param {string} args.carColor - user's car color
 * @param {string} args.licensePlate - user's license plate
 * @param {string} args.phone - optional. user's phone number, format: "202-401-9200"
 */
FlyBuy.prototype.updateCustomer = function (success, error, args) {
  exec(success, error, "FlyBuy", "updateCustomer", [args]);
};

/**
 * @function loginWithToken
 * Login with customer.apiToken
 * @param {function(string)} success - callback, PluginResult.Status.OK as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {string} args - required. customer.apiToken
 */
FlyBuy.prototype.loginWithToken = function (success, error, args) {
  exec(success, error, "FlyBuy", "loginWithToken", [args]);
};
/**
 * @function logout
 * Logout current customer
 * @param {function(string)} success - callback, PluginResult.Status.OK as callback param
 * @param {function(string)} error - callback, error message as callback param
 */
FlyBuy.prototype.logout = function (success, error) {
  exec(success, error, "FlyBuy", "logout");
};

/**
 * @function claimCode
 * Claim order with redemption code
 * @param {function(object)} success - callback, order object as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {object} args - order redemption code
 * @param {string} args.redemptionCode - required. order redemption code
 * @param {string} args.pickupType - optional. "curbside", "pickup", or "delivery"
 */
FlyBuy.prototype.claimCode = function (success, error, args) {
  exec(success, error, "FlyBuy", "claimCode", [args]);
};

/**
 * @function updateCustomerState
 * Update customer state with given order id
 * @param {function(object)} success - callback, order object as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {object} args
 * @param {int} args.orderId - required. order id
 * @param {string} args.customerState - Customer State ENUM Values
 */
FlyBuy.prototype.updateCustomerState = function (success, error, args) {
  exec(success, error, "FlyBuy", "updateCustomerState", [args]);
};

/**
 * @function fetchOrders
 * Fetch all orders for current customer
 * @param {function(array)} success - callback, array of order objects as callback param
 * @param {function(string)} error - callback, error message as callback param
 */
FlyBuy.prototype.fetchOrders = function (success, error) {
  exec(success, error, "FlyBuy", "fetchOrders");
};

/**
 * @function fetchOrderWithRedemptionCode
 * Fetch unclaimed order that contains the redemption code.
 * Attempt to fetch a claimed order using this method will get an error message of "order not found" from FlyBuy SDK.
 * @param {function(object)} success - callback, order object as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {string} args - required. order redemption code
 */
FlyBuy.prototype.fetchOrderWithRedemptionCode = function (success, error, args) {
  exec(success, error, "FlyBuy", "fetchOrderWithRedemptionCode", [args]);
};

/**
 * @function getOrdersByStatus
 * Fetch orders by status: "all", "open", or "closed".
 * @param {function(array)} success - callback, array of order objects as callback param
 * @param {function(string)} error - callback, error message as callback param
 * @param {string} args - required. accepts strings:  "all", "open", "closed".  unrecognized strings will return all orders
 */
FlyBuy.prototype.getOrdersByStatus = function (success, error, args) {
  exec(success, error, "FlyBuy", "getOrdersByStatus", [args]);
};

var FlyBuy = new FlyBuy();
module.exports = FlyBuy;
