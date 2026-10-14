package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy incident entry. */
data class LegacyIncidentEntryView(
    var code: String,
    var label: String,
    var country: String,
    var archived: String,
    var timezoneText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toLegacyIncidentEntryView(item: LegacyIncidentEntry): LegacyIncidentEntryView {
    return LegacyIncidentEntryView(
        code = item.code,
        label = item.label,
        country = truncate(item.country, 120),
        archived = "${item.archived}",
        timezoneText = item.timezone,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIncidentEntryViews(items: MutableList<LegacyIncidentEntry>): MutableList<LegacyIncidentEntryView> {
    val views = mutableListOf<LegacyIncidentEntryView>()
    for (item in items) {
        views.add(toLegacyIncidentEntryView(item))
    }
    return views
}

fun sumLegacyIncidentEntryView(view: LegacyIncidentEntryView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
