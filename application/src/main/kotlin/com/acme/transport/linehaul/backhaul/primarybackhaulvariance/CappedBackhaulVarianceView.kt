package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped backhaul variance. */
data class CappedBackhaulVarianceView(
    var codeText: String,
    var title: String,
    var discountText: String,
    var floorValue: String,
    var durationMinutes: String,
    var name: String,
    var statusLabel: String,
)

fun toCappedBackhaulVarianceView(item: CappedBackhaulVariance): CappedBackhaulVarianceView {
    return CappedBackhaulVarianceView(
        codeText = item.code,
        title = truncate(item.title, 64),
        discountText = "%.2f".format(item.discount),
        floorValue = "%.2f".format(item.floorValue),
        durationMinutes = item.durationMinutes.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBackhaulVarianceViews(items: MutableList<CappedBackhaulVariance>): MutableList<CappedBackhaulVarianceView> {
    val views = mutableListOf<CappedBackhaulVarianceView>()
    for (item in items) {
        views.add(toCappedBackhaulVarianceView(item))
    }
    return views
}

fun sumCappedBackhaulVarianceView(view: CappedBackhaulVarianceView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDiscount + parsedFloorValue + parsedDurationMinutes.toDouble()
}
