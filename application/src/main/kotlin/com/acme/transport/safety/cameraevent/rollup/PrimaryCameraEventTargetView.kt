package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary camera event target. */
data class PrimaryCameraEventTargetView(
    var code: String,
    var titleText: String,
    var floorValueText: String,
    var categoryText: String,
    var marginText: String,
    var statusLabel: String,
)

fun toPrimaryCameraEventTargetView(item: PrimaryCameraEventTarget): PrimaryCameraEventTargetView {
    return PrimaryCameraEventTargetView(
        code = item.code,
        titleText = item.title,
        floorValueText = "%.2f".format(item.floorValue),
        categoryText = truncate(item.category, 80),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCameraEventTargetViews(items: MutableList<PrimaryCameraEventTarget>): MutableList<PrimaryCameraEventTargetView> {
    val views = mutableListOf<PrimaryCameraEventTargetView>()
    for (item in items) {
        views.add(toPrimaryCameraEventTargetView(item))
    }
    return views
}

fun sumPrimaryCameraEventTargetView(view: PrimaryCameraEventTargetView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedFloorValue + parsedMargin
}
