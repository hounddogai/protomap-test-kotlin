package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual fuel surcharge segment. */
data class ManualFuelSurchargeSegmentView(
    var codeText: String,
    var title: String,
    var minimum: String,
    var stopCount: String,
    var expiresAt: String,
    var channel: String,
    var loadKg: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toManualFuelSurchargeSegmentView(item: ManualFuelSurchargeSegment): ManualFuelSurchargeSegmentView {
    return ManualFuelSurchargeSegmentView(
        codeText = truncate(item.code, 120),
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        stopCount = item.stopCount.toString(),
        expiresAt = item.expiresAt.toString(),
        channel = item.channel,
        loadKg = "%.2f".format(item.loadKg),
        routeCodeText = truncate(item.routeCode, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualFuelSurchargeSegmentViews(items: MutableList<ManualFuelSurchargeSegment>): MutableList<ManualFuelSurchargeSegmentView> {
    val views = mutableListOf<ManualFuelSurchargeSegmentView>()
    for (item in items) {
        views.add(toManualFuelSurchargeSegmentView(item))
    }
    return views
}

fun sumManualFuelSurchargeSegmentView(view: ManualFuelSurchargeSegmentView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMinimum + parsedStopCount.toDouble() + parsedExpiresAt.toDouble()
}
