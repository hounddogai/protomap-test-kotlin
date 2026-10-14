package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual drop record. */
data class ManualDropRecordView(
    var code: String,
    var labelText: String,
    var createdAtText: String,
    var budget: String,
    var ownerIdText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toManualDropRecordView(item: ManualDropRecord): ManualDropRecordView {
    return ManualDropRecordView(
        code = item.code,
        labelText = item.label,
        createdAtText = item.createdAt.toString(),
        budget = "%.2f".format(item.budget),
        ownerIdText = truncate(item.ownerId, 40),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDropRecordViews(items: MutableList<ManualDropRecord>): MutableList<ManualDropRecordView> {
    val views = mutableListOf<ManualDropRecordView>()
    for (item in items) {
        views.add(toManualDropRecordView(item))
    }
    return views
}

fun sumManualDropRecordView(view: ManualDropRecordView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedCreatedAt.toDouble() + parsedBudget
}
