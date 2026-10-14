package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted accessorial adjustment. */
data class WeightedAccessorialAdjustmentView(
    var code: String,
    var name: String,
    var batchSizeText: String,
    var currency: String,
    var routeCode: String,
    var amountText: String,
    var statusLabel: String,
)

fun toWeightedAccessorialAdjustmentView(item: WeightedAccessorialAdjustment): WeightedAccessorialAdjustmentView {
    return WeightedAccessorialAdjustmentView(
        code = item.code,
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        currency = item.currency,
        routeCode = truncate(item.routeCode, 64),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAccessorialAdjustmentViews(items: MutableList<WeightedAccessorialAdjustment>): MutableList<WeightedAccessorialAdjustmentView> {
    val views = mutableListOf<WeightedAccessorialAdjustmentView>()
    for (item in items) {
        views.add(toWeightedAccessorialAdjustmentView(item))
    }
    return views
}

fun sumWeightedAccessorialAdjustmentView(view: WeightedAccessorialAdjustmentView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedBatchSize.toDouble() + parsedAmount
}
