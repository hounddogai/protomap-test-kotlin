package com.acme.transport.emissions.idling.quota

/** How screens and exports show a idling estimate. */
data class IdlingEstimateView(
    var code: String,
    var name: String,
    var costText: String,
    var unit: String,
    var odometer: String,
    var statusLabel: String,
)

fun toIdlingEstimateView(item: IdlingEstimate): IdlingEstimateView {
    return IdlingEstimateView(
        code = item.code,
        name = item.name,
        costText = "%.2f".format(item.cost),
        unit = item.unit,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingEstimateViews(items: MutableList<IdlingEstimate>): MutableList<IdlingEstimateView> {
    val views = mutableListOf<IdlingEstimateView>()
    for (item in items) {
        views.add(toIdlingEstimateView(item))
    }
    return views
}

fun sumIdlingEstimateView(view: IdlingEstimateView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedCost + parsedOdometer.toDouble()
}
