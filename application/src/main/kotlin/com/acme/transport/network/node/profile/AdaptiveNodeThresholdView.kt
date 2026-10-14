package com.acme.transport.network.node.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive node threshold. */
data class AdaptiveNodeThresholdView(
    var code: String,
    var nameText: String,
    var locale: String,
    var margin: String,
    var discount: String,
    var odometerText: String,
    var fuelLiters: String,
    var title: String,
    var driverIdText: String,
    var lane: String,
    var currency: String,
    var statusLabel: String,
)

fun toAdaptiveNodeThresholdView(item: AdaptiveNodeThreshold): AdaptiveNodeThresholdView {
    return AdaptiveNodeThresholdView(
        code = item.code,
        nameText = item.name,
        locale = truncate(item.locale, 40),
        margin = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        odometerText = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        title = item.title,
        driverIdText = item.driverId,
        lane = truncate(item.lane, 40),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNodeThresholdViews(items: MutableList<AdaptiveNodeThreshold>): MutableList<AdaptiveNodeThresholdView> {
    val views = mutableListOf<AdaptiveNodeThresholdView>()
    for (item in items) {
        views.add(toAdaptiveNodeThresholdView(item))
    }
    return views
}

fun sumAdaptiveNodeThresholdView(view: AdaptiveNodeThresholdView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedMargin + parsedDiscount + parsedOdometer.toDouble()
}
