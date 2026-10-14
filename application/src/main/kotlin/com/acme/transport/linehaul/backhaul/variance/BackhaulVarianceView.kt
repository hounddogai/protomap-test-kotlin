package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul variance. */
data class BackhaulVarianceView(
    var code: String,
    var titleText: String,
    var stopCountText: String,
    var quantityText: String,
    var minimum: String,
    var categoryText: String,
    var amount: String,
    var statusLabel: String,
)

fun toBackhaulVarianceView(item: BackhaulVariance): BackhaulVarianceView {
    return BackhaulVarianceView(
        code = item.code,
        titleText = truncate(item.title, 40),
        stopCountText = item.stopCount.toString(),
        quantityText = item.quantity.toString(),
        minimum = "%.2f".format(item.minimum),
        categoryText = truncate(item.category, 40),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulVarianceViews(items: MutableList<BackhaulVariance>): MutableList<BackhaulVarianceView> {
    val views = mutableListOf<BackhaulVarianceView>()
    for (item in items) {
        views.add(toBackhaulVarianceView(item))
    }
    return views
}

fun sumBackhaulVarianceView(view: BackhaulVarianceView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStopCount.toDouble() + parsedQuantity.toDouble() + parsedMinimum
}
