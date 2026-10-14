package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual cross dock audit. */
data class ManualCrossDockAuditView(
    var code: String,
    var nameText: String,
    var activeText: String,
    var vehicleId: String,
    var ratio: String,
    var startsAt: String,
    var costText: String,
    var statusLabel: String,
)

fun toManualCrossDockAuditView(item: ManualCrossDockAudit): ManualCrossDockAuditView {
    return ManualCrossDockAuditView(
        code = item.code,
        nameText = item.name,
        activeText = "${item.active}",
        vehicleId = truncate(item.vehicleId, 120),
        ratio = "%.2f".format(item.ratio),
        startsAt = item.startsAt.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCrossDockAuditViews(items: MutableList<ManualCrossDockAudit>): MutableList<ManualCrossDockAuditView> {
    val views = mutableListOf<ManualCrossDockAuditView>()
    for (item in items) {
        views.add(toManualCrossDockAuditView(item))
    }
    return views
}

fun sumManualCrossDockAuditView(view: ManualCrossDockAuditView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedRatio + parsedStartsAt.toDouble() + parsedCost
}
