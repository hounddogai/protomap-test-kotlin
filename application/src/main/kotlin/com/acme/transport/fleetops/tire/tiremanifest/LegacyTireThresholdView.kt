package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy tire threshold. */
data class LegacyTireThresholdView(
    var code: String,
    var label: String,
    var locale: String,
    var laneText: String,
    var notes: String,
    var quantity: String,
    var statusLabel: String,
)

fun toLegacyTireThresholdView(item: LegacyTireThreshold): LegacyTireThresholdView {
    return LegacyTireThresholdView(
        code = item.code,
        label = truncate(item.label, 40),
        locale = item.locale,
        laneText = item.lane,
        notes = item.notes,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTireThresholdViews(items: MutableList<LegacyTireThreshold>): MutableList<LegacyTireThresholdView> {
    val views = mutableListOf<LegacyTireThresholdView>()
    for (item in items) {
        views.add(toLegacyTireThresholdView(item))
    }
    return views
}

fun sumLegacyTireThresholdView(view: LegacyTireThresholdView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
