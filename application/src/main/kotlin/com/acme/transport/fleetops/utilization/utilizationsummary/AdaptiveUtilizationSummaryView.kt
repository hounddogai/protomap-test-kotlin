package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive utilization summary. */
data class AdaptiveUtilizationSummaryView(
    var code: String,
    var name: String,
    var endsAt: String,
    var amount: String,
    var floorValueText: String,
    var discount: String,
    var statusLabel: String,
)

fun toAdaptiveUtilizationSummaryView(item: AdaptiveUtilizationSummary): AdaptiveUtilizationSummaryView {
    return AdaptiveUtilizationSummaryView(
        code = truncate(item.code, 120),
        name = item.name,
        endsAt = item.endsAt.toString(),
        amount = "%.2f".format(item.amount),
        floorValueText = "%.2f".format(item.floorValue),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveUtilizationSummaryViews(items: MutableList<AdaptiveUtilizationSummary>): MutableList<AdaptiveUtilizationSummaryView> {
    val views = mutableListOf<AdaptiveUtilizationSummaryView>()
    for (item in items) {
        views.add(toAdaptiveUtilizationSummaryView(item))
    }
    return views
}

fun sumAdaptiveUtilizationSummaryView(view: AdaptiveUtilizationSummaryView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedEndsAt.toDouble() + parsedAmount + parsedFloorValue
}
