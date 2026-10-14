package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary service area threshold. */
data class PrimaryServiceAreaThresholdView(
    var codeText: String,
    var name: String,
    var windowDays: String,
    var region: String,
    var checksum: String,
    var maximum: String,
    var quantityText: String,
    var margin: String,
    var statusLabel: String,
)

fun toPrimaryServiceAreaThresholdView(item: PrimaryServiceAreaThreshold): PrimaryServiceAreaThresholdView {
    return PrimaryServiceAreaThresholdView(
        codeText = item.code,
        name = truncate(item.name, 80),
        windowDays = item.windowDays.toString(),
        region = truncate(item.region, 40),
        checksum = item.checksum,
        maximum = "%.2f".format(item.maximum),
        quantityText = item.quantity.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServiceAreaThresholdViews(items: MutableList<PrimaryServiceAreaThreshold>): MutableList<PrimaryServiceAreaThresholdView> {
    val views = mutableListOf<PrimaryServiceAreaThresholdView>()
    for (item in items) {
        views.add(toPrimaryServiceAreaThresholdView(item))
    }
    return views
}

fun sumPrimaryServiceAreaThresholdView(view: PrimaryServiceAreaThresholdView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedWindowDays.toDouble() + parsedMaximum + parsedQuantity.toDouble()
}
