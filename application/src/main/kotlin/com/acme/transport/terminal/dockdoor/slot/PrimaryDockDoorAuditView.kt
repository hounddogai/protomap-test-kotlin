package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary dock door audit. */
data class PrimaryDockDoorAuditView(
    var code: String,
    var label: String,
    var trailerId: String,
    var batchSize: String,
    var budget: String,
    var channel: String,
    var statusLabel: String,
)

fun toPrimaryDockDoorAuditView(item: PrimaryDockDoorAudit): PrimaryDockDoorAuditView {
    return PrimaryDockDoorAuditView(
        code = item.code,
        label = item.label,
        trailerId = item.trailerId,
        batchSize = item.batchSize.toString(),
        budget = "%.2f".format(item.budget),
        channel = truncate(item.channel, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDockDoorAuditViews(items: MutableList<PrimaryDockDoorAudit>): MutableList<PrimaryDockDoorAuditView> {
    val views = mutableListOf<PrimaryDockDoorAuditView>()
    for (item in items) {
        views.add(toPrimaryDockDoorAuditView(item))
    }
    return views
}

fun sumPrimaryDockDoorAuditView(view: PrimaryDockDoorAuditView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedBatchSize.toDouble() + parsedBudget
}
