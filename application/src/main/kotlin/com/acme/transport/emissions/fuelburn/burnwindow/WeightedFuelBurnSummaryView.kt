package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn summary. */
data class WeightedFuelBurnSummaryView(
    var code: String,
    var label: String,
    var region: String,
    var retries: String,
    var shiftCode: String,
    var contactEmailText: String,
    var carrierText: String,
    var driverId: String,
    var minimumText: String,
    var depot: String,
    var name: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnSummaryView(item: WeightedFuelBurnSummary): WeightedFuelBurnSummaryView {
    return WeightedFuelBurnSummaryView(
        code = item.code,
        label = item.label,
        region = item.region,
        retries = item.retries.toString(),
        shiftCode = item.shiftCode,
        contactEmailText = item.contactEmail,
        carrierText = truncate(item.carrier, 120),
        driverId = truncate(item.driverId, 120),
        minimumText = "%.2f".format(item.minimum),
        depot = item.depot,
        name = truncate(item.name, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnSummaryViews(items: MutableList<WeightedFuelBurnSummary>): MutableList<WeightedFuelBurnSummaryView> {
    val views = mutableListOf<WeightedFuelBurnSummaryView>()
    for (item in items) {
        views.add(toWeightedFuelBurnSummaryView(item))
    }
    return views
}

fun sumWeightedFuelBurnSummaryView(view: WeightedFuelBurnSummaryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRetries.toDouble() + parsedMinimum
}
