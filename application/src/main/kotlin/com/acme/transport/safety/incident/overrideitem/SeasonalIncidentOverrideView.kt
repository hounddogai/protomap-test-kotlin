package com.acme.transport.safety.incident.overrideitem

/** How screens and exports show a seasonal incident override. */
data class SeasonalIncidentOverrideView(
    var code: String,
    var title: String,
    var timezone: String,
    var country: String,
    var shiftCode: String,
    var threshold: String,
    var pallets: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toSeasonalIncidentOverrideView(item: SeasonalIncidentOverride): SeasonalIncidentOverrideView {
    return SeasonalIncidentOverrideView(
        code = item.code,
        title = item.title,
        timezone = item.timezone,
        country = item.country,
        shiftCode = item.shiftCode,
        threshold = "%.2f".format(item.threshold),
        pallets = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalIncidentOverrideViews(items: MutableList<SeasonalIncidentOverride>): MutableList<SeasonalIncidentOverrideView> {
    val views = mutableListOf<SeasonalIncidentOverrideView>()
    for (item in items) {
        views.add(toSeasonalIncidentOverrideView(item))
    }
    return views
}

fun sumSeasonalIncidentOverrideView(view: SeasonalIncidentOverrideView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedThreshold + parsedPallets.toDouble() + parsedEtaMinutes.toDouble()
}
