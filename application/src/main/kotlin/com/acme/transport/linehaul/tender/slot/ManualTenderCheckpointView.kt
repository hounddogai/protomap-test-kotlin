package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tender checkpoint. */
data class ManualTenderCheckpointView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var segmentText: String,
    var pallets: String,
    var budget: String,
    var summary: String,
    var unitText: String,
    var statusLabel: String,
)

fun toManualTenderCheckpointView(item: ManualTenderCheckpoint): ManualTenderCheckpointView {
    return ManualTenderCheckpointView(
        code = item.code,
        label = truncate(item.label, 120),
        contactEmail = item.contactEmail,
        segmentText = item.segment,
        pallets = item.pallets.toString(),
        budget = "%.2f".format(item.budget),
        summary = item.summary,
        unitText = truncate(item.unit, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTenderCheckpointViews(items: MutableList<ManualTenderCheckpoint>): MutableList<ManualTenderCheckpointView> {
    val views = mutableListOf<ManualTenderCheckpointView>()
    for (item in items) {
        views.add(toManualTenderCheckpointView(item))
    }
    return views
}

fun sumManualTenderCheckpointView(view: ManualTenderCheckpointView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedPallets.toDouble() + parsedBudget
}
