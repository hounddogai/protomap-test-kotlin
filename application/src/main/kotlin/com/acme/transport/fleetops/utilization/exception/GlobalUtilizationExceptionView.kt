package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global utilization exception. */
data class GlobalUtilizationExceptionView(
    var code: String,
    var name: String,
    var discount: String,
    var ownerId: String,
    var fuelLiters: String,
    var channel: String,
    var driverIdText: String,
    var contactEmail: String,
    var depot: String,
    var statusLabel: String,
)

fun toGlobalUtilizationExceptionView(item: GlobalUtilizationException): GlobalUtilizationExceptionView {
    return GlobalUtilizationExceptionView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        discount = "%.2f".format(item.discount),
        ownerId = item.ownerId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        channel = truncate(item.channel, 40),
        driverIdText = item.driverId,
        contactEmail = truncate(item.contactEmail, 64),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalUtilizationExceptionViews(items: MutableList<GlobalUtilizationException>): MutableList<GlobalUtilizationExceptionView> {
    val views = mutableListOf<GlobalUtilizationExceptionView>()
    for (item in items) {
        views.add(toGlobalUtilizationExceptionView(item))
    }
    return views
}

fun sumGlobalUtilizationExceptionView(view: GlobalUtilizationExceptionView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedDiscount + parsedFuelLiters
}
