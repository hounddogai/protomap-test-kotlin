package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional carbon report adjustment. */
data class RegionalCarbonReportAdjustmentView(
    var code: String,
    var name: String,
    var cost: String,
    var quantity: String,
    var versionText: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportAdjustmentView(item: RegionalCarbonReportAdjustment): RegionalCarbonReportAdjustmentView {
    return RegionalCarbonReportAdjustmentView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 64),
        cost = "%.2f".format(item.cost),
        quantity = item.quantity.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportAdjustmentViews(items: MutableList<RegionalCarbonReportAdjustment>): MutableList<RegionalCarbonReportAdjustmentView> {
    val views = mutableListOf<RegionalCarbonReportAdjustmentView>()
    for (item in items) {
        views.add(toRegionalCarbonReportAdjustmentView(item))
    }
    return views
}

fun sumRegionalCarbonReportAdjustmentView(view: RegionalCarbonReportAdjustmentView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedCost + parsedQuantity.toDouble() + parsedVersion.toDouble()
}
