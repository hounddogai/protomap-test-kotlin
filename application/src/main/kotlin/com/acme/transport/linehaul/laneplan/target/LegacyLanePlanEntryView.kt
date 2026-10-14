package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy lane plan entry. */
data class LegacyLanePlanEntryView(
    var codeText: String,
    var label: String,
    var vehicleId: String,
    var cost: String,
    var carrier: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toLegacyLanePlanEntryView(item: LegacyLanePlanEntry): LegacyLanePlanEntryView {
    return LegacyLanePlanEntryView(
        codeText = item.code,
        label = item.label,
        vehicleId = item.vehicleId,
        cost = "%.2f".format(item.cost),
        carrier = truncate(item.carrier, 64),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLanePlanEntryViews(items: MutableList<LegacyLanePlanEntry>): MutableList<LegacyLanePlanEntryView> {
    val views = mutableListOf<LegacyLanePlanEntryView>()
    for (item in items) {
        views.add(toLegacyLanePlanEntryView(item))
    }
    return views
}

fun sumLegacyLanePlanEntryView(view: LegacyLanePlanEntryView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCost + parsedExpiresAt.toDouble()
}
