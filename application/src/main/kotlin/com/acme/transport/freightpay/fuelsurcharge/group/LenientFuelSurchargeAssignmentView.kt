package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient fuel surcharge assignment. */
data class LenientFuelSurchargeAssignmentView(
    var code: String,
    var label: String,
    var batchSize: String,
    var thresholdText: String,
    var rate: String,
    var shiftCode: String,
    var depotText: String,
    var reviewer: String,
    var windowDays: String,
    var createdAt: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toLenientFuelSurchargeAssignmentView(item: LenientFuelSurchargeAssignment): LenientFuelSurchargeAssignmentView {
    return LenientFuelSurchargeAssignmentView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        batchSize = item.batchSize.toString(),
        thresholdText = "%.2f".format(item.threshold),
        rate = "%.2f".format(item.rate),
        shiftCode = item.shiftCode,
        depotText = item.depot,
        reviewer = truncate(item.reviewer, 40),
        windowDays = item.windowDays.toString(),
        createdAt = item.createdAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelSurchargeAssignmentViews(items: MutableList<LenientFuelSurchargeAssignment>): MutableList<LenientFuelSurchargeAssignmentView> {
    val views = mutableListOf<LenientFuelSurchargeAssignmentView>()
    for (item in items) {
        views.add(toLenientFuelSurchargeAssignmentView(item))
    }
    return views
}

fun sumLenientFuelSurchargeAssignmentView(view: LenientFuelSurchargeAssignmentView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedBatchSize.toDouble() + parsedThreshold + parsedRate
}
