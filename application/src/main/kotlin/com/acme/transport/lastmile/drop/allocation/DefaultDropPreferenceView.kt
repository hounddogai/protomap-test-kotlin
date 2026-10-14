package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default drop preference. */
data class DefaultDropPreferenceView(
    var code: String,
    var titleText: String,
    var quantityText: String,
    var name: String,
    var floorValue: String,
    var stopCountText: String,
    var summaryText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toDefaultDropPreferenceView(item: DefaultDropPreference): DefaultDropPreferenceView {
    return DefaultDropPreferenceView(
        code = truncate(item.code, 64),
        titleText = item.title,
        quantityText = item.quantity.toString(),
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        stopCountText = item.stopCount.toString(),
        summaryText = item.summary,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDropPreferenceViews(items: MutableList<DefaultDropPreference>): MutableList<DefaultDropPreferenceView> {
    val views = mutableListOf<DefaultDropPreferenceView>()
    for (item in items) {
        views.add(toDefaultDropPreferenceView(item))
    }
    return views
}

fun sumDefaultDropPreferenceView(view: DefaultDropPreferenceView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedQuantity.toDouble() + parsedFloorValue + parsedStopCount.toDouble()
}
