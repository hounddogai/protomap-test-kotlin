package com.acme.transport.emissions.carbonreport.adjustment

/** How screens and exports show a carbon report adjustment. */
data class CarbonReportAdjustmentView(
    var codeText: String,
    var name: String,
    var maximum: String,
    var trailerId: String,
    var weight: String,
    var shiftCode: String,
    var phone: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toCarbonReportAdjustmentView(item: CarbonReportAdjustment): CarbonReportAdjustmentView {
    return CarbonReportAdjustmentView(
        codeText = item.code,
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        trailerId = item.trailerId,
        weight = "%.2f".format(item.weight),
        shiftCode = item.shiftCode,
        phone = item.phone,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportAdjustmentViews(items: MutableList<CarbonReportAdjustment>): MutableList<CarbonReportAdjustmentView> {
    val views = mutableListOf<CarbonReportAdjustmentView>()
    for (item in items) {
        views.add(toCarbonReportAdjustmentView(item))
    }
    return views
}

fun sumCarbonReportAdjustmentView(view: CarbonReportAdjustmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedMaximum + parsedWeight + parsedUpdatedAt.toDouble()
}
