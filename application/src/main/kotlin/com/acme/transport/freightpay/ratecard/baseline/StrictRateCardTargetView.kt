package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict rate card target. */
data class StrictRateCardTargetView(
    var code: String,
    var title: String,
    var dock: String,
    var ownerId: String,
    var floorValue: String,
    var externalRef: String,
    var limit: String,
    var statusLabel: String,
)

fun toStrictRateCardTargetView(item: StrictRateCardTarget): StrictRateCardTargetView {
    return StrictRateCardTargetView(
        code = item.code,
        title = truncate(item.title, 40),
        dock = truncate(item.dock, 80),
        ownerId = truncate(item.ownerId, 64),
        floorValue = "%.2f".format(item.floorValue),
        externalRef = item.externalRef,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRateCardTargetViews(items: MutableList<StrictRateCardTarget>): MutableList<StrictRateCardTargetView> {
    val views = mutableListOf<StrictRateCardTargetView>()
    for (item in items) {
        views.add(toStrictRateCardTargetView(item))
    }
    return views
}

fun sumStrictRateCardTargetView(view: StrictRateCardTargetView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedFloorValue + parsedLimit.toDouble()
}
