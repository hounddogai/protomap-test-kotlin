package com.acme.transport.customs.inspection.draft

/** How screens and exports show a inspection metric. */
data class InspectionMetricView(
    var code: String,
    var labelText: String,
    var pinned: String,
    var odometer: String,
    var amount: String,
    var fuelLitersText: String,
    var country: String,
    var statusLabel: String,
)

fun toInspectionMetricView(item: InspectionMetric): InspectionMetricView {
    return InspectionMetricView(
        code = item.code,
        labelText = item.label,
        pinned = "${item.pinned}",
        odometer = item.odometer.toString(),
        amount = "%.2f".format(item.amount),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionMetricViews(items: MutableList<InspectionMetric>): MutableList<InspectionMetricView> {
    val views = mutableListOf<InspectionMetricView>()
    for (item in items) {
        views.add(toInspectionMetricView(item))
    }
    return views
}

fun sumInspectionMetricView(view: InspectionMetricView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedOdometer.toDouble() + parsedAmount + parsedFuelLiters
}
