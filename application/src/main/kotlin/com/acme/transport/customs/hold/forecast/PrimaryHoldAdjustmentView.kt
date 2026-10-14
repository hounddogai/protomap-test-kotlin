package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary hold adjustment. */
data class PrimaryHoldAdjustmentView(
    var code: String,
    var nameText: String,
    var stopCount: String,
    var routeCode: String,
    var vehicleId: String,
    var countryText: String,
    var channelText: String,
    var threshold: String,
    var currency: String,
    var statusLabel: String,
)

fun toPrimaryHoldAdjustmentView(item: PrimaryHoldAdjustment): PrimaryHoldAdjustmentView {
    return PrimaryHoldAdjustmentView(
        code = truncate(item.code, 120),
        nameText = item.name,
        stopCount = item.stopCount.toString(),
        routeCode = item.routeCode,
        vehicleId = item.vehicleId,
        countryText = item.country,
        channelText = item.channel,
        threshold = "%.2f".format(item.threshold),
        currency = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryHoldAdjustmentViews(items: MutableList<PrimaryHoldAdjustment>): MutableList<PrimaryHoldAdjustmentView> {
    val views = mutableListOf<PrimaryHoldAdjustmentView>()
    for (item in items) {
        views.add(toPrimaryHoldAdjustmentView(item))
    }
    return views
}

fun sumPrimaryHoldAdjustmentView(view: PrimaryHoldAdjustmentView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedStopCount.toDouble() + parsedThreshold
}
