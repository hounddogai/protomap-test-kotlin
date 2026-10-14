package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization batch. */
data class UtilizationBatchView(
    var code: String,
    var title: String,
    var costText: String,
    var discountText: String,
    var pallets: String,
    var etaMinutesText: String,
    var amountText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toUtilizationBatchView(item: UtilizationBatch): UtilizationBatchView {
    return UtilizationBatchView(
        code = item.code,
        title = truncate(item.title, 64),
        costText = "%.2f".format(item.cost),
        discountText = "%.2f".format(item.discount),
        pallets = item.pallets.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        amountText = "%.2f".format(item.amount),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationBatchViews(items: MutableList<UtilizationBatch>): MutableList<UtilizationBatchView> {
    val views = mutableListOf<UtilizationBatchView>()
    for (item in items) {
        views.add(toUtilizationBatchView(item))
    }
    return views
}

fun sumUtilizationBatchView(view: UtilizationBatchView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedCost + parsedDiscount + parsedPallets.toDouble()
}
