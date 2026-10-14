package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy clearance checkpoint. */
data class LegacyClearanceCheckpointView(
    var code: String,
    var name: String,
    var ownerIdText: String,
    var label: String,
    var summary: String,
    var cost: String,
    var statusLabel: String,
)

fun toLegacyClearanceCheckpointView(item: LegacyClearanceCheckpoint): LegacyClearanceCheckpointView {
    return LegacyClearanceCheckpointView(
        code = truncate(item.code, 120),
        name = item.name,
        ownerIdText = item.ownerId,
        label = item.label,
        summary = truncate(item.summary, 120),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyClearanceCheckpointViews(items: MutableList<LegacyClearanceCheckpoint>): MutableList<LegacyClearanceCheckpointView> {
    val views = mutableListOf<LegacyClearanceCheckpointView>()
    for (item in items) {
        views.add(toLegacyClearanceCheckpointView(item))
    }
    return views
}

fun sumLegacyClearanceCheckpointView(view: LegacyClearanceCheckpointView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}
