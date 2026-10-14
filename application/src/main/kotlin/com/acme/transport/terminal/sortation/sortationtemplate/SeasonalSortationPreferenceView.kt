package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal sortation preference. */
data class SeasonalSortationPreferenceView(
    var code: String,
    var name: String,
    var source: String,
    var label: String,
    var distanceKmText: String,
    var durationMinutes: String,
    var notes: String,
    var odometer: String,
    var category: String,
    var quantity: String,
    var statusLabel: String,
)

fun toSeasonalSortationPreferenceView(item: SeasonalSortationPreference): SeasonalSortationPreferenceView {
    return SeasonalSortationPreferenceView(
        code = item.code,
        name = item.name,
        source = item.source,
        label = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        durationMinutes = item.durationMinutes.toString(),
        notes = truncate(item.notes, 64),
        odometer = item.odometer.toString(),
        category = item.category,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalSortationPreferenceViews(items: MutableList<SeasonalSortationPreference>): MutableList<SeasonalSortationPreferenceView> {
    val views = mutableListOf<SeasonalSortationPreferenceView>()
    for (item in items) {
        views.add(toSeasonalSortationPreferenceView(item))
    }
    return views
}

fun sumSeasonalSortationPreferenceView(view: SeasonalSortationPreferenceView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedDistanceKm + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
