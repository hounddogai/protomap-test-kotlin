package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual zone checkpoint. */
data class ManualZoneCheckpointView(
    var code: String,
    var titleText: String,
    var budget: String,
    var locale: String,
    var active: String,
    var categoryText: String,
    var label: String,
    var statusLabel: String,
)

fun toManualZoneCheckpointView(item: ManualZoneCheckpoint): ManualZoneCheckpointView {
    return ManualZoneCheckpointView(
        code = truncate(item.code, 64),
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        locale = item.locale,
        active = "${item.active}",
        categoryText = item.category,
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualZoneCheckpointViews(items: MutableList<ManualZoneCheckpoint>): MutableList<ManualZoneCheckpointView> {
    val views = mutableListOf<ManualZoneCheckpointView>()
    for (item in items) {
        views.add(toManualZoneCheckpointView(item))
    }
    return views
}

fun sumManualZoneCheckpointView(view: ManualZoneCheckpointView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
