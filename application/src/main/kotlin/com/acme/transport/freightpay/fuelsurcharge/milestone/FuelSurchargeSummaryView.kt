package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge summary. */
data class FuelSurchargeSummaryView(
    var code: String,
    var label: String,
    var expiresAtText: String,
    var thresholdText: String,
    var priorityText: String,
    var maximum: String,
    var vehicleId: String,
    var summary: String,
    var statusLabel: String,
)

fun toFuelSurchargeSummaryView(item: FuelSurchargeSummary): FuelSurchargeSummaryView {
    return FuelSurchargeSummaryView(
        code = truncate(item.code, 120),
        label = item.label,
        expiresAtText = item.expiresAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        priorityText = item.priority.toString(),
        maximum = "%.2f".format(item.maximum),
        vehicleId = truncate(item.vehicleId, 40),
        summary = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeSummaryViews(items: MutableList<FuelSurchargeSummary>): MutableList<FuelSurchargeSummaryView> {
    val views = mutableListOf<FuelSurchargeSummaryView>()
    for (item in items) {
        views.add(toFuelSurchargeSummaryView(item))
    }
    return views
}

fun sumFuelSurchargeSummaryView(view: FuelSurchargeSummaryView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedExpiresAt.toDouble() + parsedThreshold + parsedPriority.toDouble()
}
