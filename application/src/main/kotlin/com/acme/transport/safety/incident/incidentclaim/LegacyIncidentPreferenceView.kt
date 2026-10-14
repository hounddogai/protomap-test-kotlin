package com.acme.transport.safety.incident.incidentclaim

/** How screens and exports show a legacy incident preference. */
data class LegacyIncidentPreferenceView(
    var codeText: String,
    var label: String,
    var description: String,
    var odometer: String,
    var timezone: String,
    var statusLabel: String,
)

fun toLegacyIncidentPreferenceView(item: LegacyIncidentPreference): LegacyIncidentPreferenceView {
    return LegacyIncidentPreferenceView(
        codeText = item.code,
        label = item.label,
        description = item.description,
        odometer = item.odometer.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIncidentPreferenceViews(items: MutableList<LegacyIncidentPreference>): MutableList<LegacyIncidentPreferenceView> {
    val views = mutableListOf<LegacyIncidentPreferenceView>()
    for (item in items) {
        views.add(toLegacyIncidentPreferenceView(item))
    }
    return views
}

fun sumLegacyIncidentPreferenceView(view: LegacyIncidentPreferenceView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
