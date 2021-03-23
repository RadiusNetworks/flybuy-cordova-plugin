/**
 * @typedef {Object} Order
 * @property {boolean} alwaysShowVehicleInfoFields
 * @property {string} arrivedAt - datetime string that the customer arrived via geolocation detection, example format "2020-11-09T21:43:51.223Z"
 * @property {string} createdAt - datetime string that the order created , example format: "2020-11-09T21:43:51.223Z"
 * @property {string} curbsideLocalizedString - string defined as curbside-  "Curbside"
 * @property {null | Object} customer - @see {@link Order.customer} below
 * @property {null | string} customerId - customer id
 * @property {boolean} customerNameEditingEnabled - boolean if customer name editable
 * @property {null | string} customerRatingComments - customer rating comments
 * @property {null | string} customerRatingValue - customer rating value
 * @property {string} customerState - customer status, available values: "CREATED", "EN_ROUTE", "NEARBY", "ARRIVED", "WAITING", "COMPLETED"
 * @property {null | string} etaAt - datetime string of estimated arrival time.
 * @property {int} id - order id
 * @property {string} orderState - order status, available values: "CREATED", "READY", "DELAYED", "CANCELLED", "COMPLETED", "GONE"
 * @property {string} partnerIdentifier - partner id
 * @property {string} pickupLocalizedString - string defined as pickup - "Pickup"
 * @property {null | string} pickupType - "curbside" or "pickup"
 * @property {boolean} pickupTypeSelectionEnabled - pickup type selection enablement
 * @property {null | Object} pickupWindow -  @see {@link Order.pickupWindow} below
 * @property {null | string} pushToken
 * @property {null | string} redeemedAt - datetime string of when customer redeemeded.  example format: "2020-11-09T21:45:07.901Z"
 * @property {string} redemptionCode - code that given to customer to redeem. example: "RYW38NA55Y"
 * @property {boolean} requireVehicleInfoIfVisible
 * @property {Object} site - @see {@link Order.site} below
 * @property {string} updatedAt - datetime string of when order last updated
 */

/**
 * @typedef {Object} Order.customer
 * @property {string} carColor - customer's car color
 * @property {string} carType - customer's car type
 * @property {string} licensePlate - customer's license plate
 * @property {string} name - customer's name
 * @property {null | string} phone - customer's phone number
 *
 */

/**
 * @typedef {Object} Order.pickupWindow
 * @property {string} end - datetime string, example format: "2020-11-12T17:58:00Z"
 * @property {string} start -  datetime string, example format: "2020-11-12T17:58:00Z"
 *
 */

/**
 * @typedef {Object} Order.site
 * @property {null | string} country - site's country
 * @property {null | string} coverPhotoUrl -  sites photo url
 * @property {string} fullAddress - site's full address
 * @property {null | Object} geofence -@see {@link Order.site.geoference} below
 * @property {string} iconUrl - site's icon url
 * @property {int} id - site's id
 * @property {string} instructions - site's instruction
 * @property {string} latitude - site's latitude
 * @property {null | string} locality - site's locality
 * @property {string} longitude - site's longitude
 * @property {string} name - site's name
 * @property {string} partnerIdentifier  - site's partner id
 * @property {string} phone - site's phone number
 * @property {null | string} postalCode -  site's postal code
 * @property {null | string} region -  site's postal region
 * @property {null | string} streetAddress - site's street address
 */

/**
 * @typedef {Object} Order.site.geoference
 * @property {double} latitude - site's latitude
 * @property {double} longitude - site's longitude
 * @property {float} radius
 */

/**
 * @typedef {Object} Customer
 * @property {string} apiToken - customer's api token, example - "xcWTZyv5faLnZGjgQ2f8hobL"
 * @property {string} carColor - customer's car color
 * @property {string} carType - customer's car type
 * @property {string} createdAt - datetime string of when account is created, example format "2020-10-21T03:26:41.640Z"
 * @property {null | string} deletedAt - datetime string of when account is deleted, example format "2020-10-21T03:26:41.640Z"
 * @property {null | string} email - customer's email
 * @property {int} id - customer's id
 * @property {string} licensePlate - customer's license plate
 * @property {string} name - customer's name
 * @property {string} phone - customer's phone number
 * @property {string} updatedAt - datetime string of when the account last updated, example format  "2020-11-11T17:33:38.291Z"
 *
 */
