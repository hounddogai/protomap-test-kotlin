package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional fuel burn record. */
data class RegionalFuelBurnRecordView(
    var codeText: String,
    var title: String,
    var countryText: String,
    var budget: String,
    var summaryText: String,
    var versionText: String,
    var durationMinutes: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRegionalFuelBurnRecordView(item: RegionalFuelBurnRecord): RegionalFuelBurnRecordView {
    return RegionalFuelBurnRecordView(
        codeText = truncate(item.code, 64),
        title = item.title,
        countryText = item.country,
        budget = "%.2f".format(item.budget),
        summaryText = item.summary,
        versionText = item.version.toString(),
        durationMinutes = item.durationMinutes.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelBurnRecordViews(items: MutableList<RegionalFuelBurnRecord>): MutableList<RegionalFuelBurnRecordView> {
    val views = mutableListOf<RegionalFuelBurnRecordView>()
    for (item in items) {
        views.add(toRegionalFuelBurnRecordView(item))
    }
    return views
}

fun sumRegionalFuelBurnRecordView(view: RegionalFuelBurnRecordView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedVersion = view.versionText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedBudget + parsedVersion.toDouble() + parsedDurationMinutes.toDouble()
}
