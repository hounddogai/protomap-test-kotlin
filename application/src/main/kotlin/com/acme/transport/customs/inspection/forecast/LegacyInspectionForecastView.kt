package com.acme.transport.customs.inspection.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection forecast. */
data class LegacyInspectionForecastView(
    var codeText: String,
    var label: String,
    var pallets: String,
    var country: String,
    var etaMinutes: String,
    var endsAt: String,
    var notes: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toLegacyInspectionForecastView(item: LegacyInspectionForecast): LegacyInspectionForecastView {
    return LegacyInspectionForecastView(
        codeText = item.code,
        label = item.label,
        pallets = item.pallets.toString(),
        country = truncate(item.country, 80),
        etaMinutes = item.etaMinutes.toString(),
        endsAt = item.endsAt.toString(),
        notes = truncate(item.notes, 40),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionForecastViews(items: MutableList<LegacyInspectionForecast>): MutableList<LegacyInspectionForecastView> {
    val views = mutableListOf<LegacyInspectionForecastView>()
    for (item in items) {
        views.add(toLegacyInspectionForecastView(item))
    }
    return views
}

fun sumLegacyInspectionForecastView(view: LegacyInspectionForecastView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedPallets.toDouble() + parsedEtaMinutes.toDouble() + parsedEndsAt.toDouble()
}
