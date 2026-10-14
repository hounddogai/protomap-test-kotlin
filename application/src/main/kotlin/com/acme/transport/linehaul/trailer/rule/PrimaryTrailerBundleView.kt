package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary trailer bundle. */
data class PrimaryTrailerBundleView(
    var code: String,
    var title: String,
    var locale: String,
    var floorValueText: String,
    var cost: String,
    var unit: String,
    var quantity: String,
    var minimumText: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toPrimaryTrailerBundleView(item: PrimaryTrailerBundle): PrimaryTrailerBundleView {
    return PrimaryTrailerBundleView(
        code = truncate(item.code, 120),
        title = item.title,
        locale = item.locale,
        floorValueText = "%.2f".format(item.floorValue),
        cost = "%.2f".format(item.cost),
        unit = truncate(item.unit, 120),
        quantity = item.quantity.toString(),
        minimumText = "%.2f".format(item.minimum),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTrailerBundleViews(items: MutableList<PrimaryTrailerBundle>): MutableList<PrimaryTrailerBundleView> {
    val views = mutableListOf<PrimaryTrailerBundleView>()
    for (item in items) {
        views.add(toPrimaryTrailerBundleView(item))
    }
    return views
}

fun sumPrimaryTrailerBundleView(view: PrimaryTrailerBundleView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedFloorValue + parsedCost + parsedQuantity.toDouble()
}
