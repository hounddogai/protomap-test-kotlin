package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary sortation assignment. */
data class PrimarySortationAssignmentView(
    var code: String,
    var label: String,
    var amountText: String,
    var routeCode: String,
    var fuelLiters: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toPrimarySortationAssignmentView(item: PrimarySortationAssignment): PrimarySortationAssignmentView {
    return PrimarySortationAssignmentView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 64),
        amountText = "%.2f".format(item.amount),
        routeCode = item.routeCode,
        fuelLiters = "%.2f".format(item.fuelLiters),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimarySortationAssignmentViews(items: MutableList<PrimarySortationAssignment>): MutableList<PrimarySortationAssignmentView> {
    val views = mutableListOf<PrimarySortationAssignmentView>()
    for (item in items) {
        views.add(toPrimarySortationAssignmentView(item))
    }
    return views
}

fun sumPrimarySortationAssignmentView(view: PrimarySortationAssignmentView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedAmount + parsedFuelLiters + parsedBatchSize.toDouble()
}
