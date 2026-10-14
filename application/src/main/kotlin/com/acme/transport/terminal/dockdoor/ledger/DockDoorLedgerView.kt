package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door ledger. */
data class DockDoorLedgerView(
    var codeText: String,
    var labelText: String,
    var priority: String,
    var depot: String,
    var capacityText: String,
    var cost: String,
    var version: String,
    var minimumText: String,
    var sequenceText: String,
    var budget: String,
    var rateText: String,
    var statusLabel: String,
)

fun toDockDoorLedgerView(item: DockDoorLedger): DockDoorLedgerView {
    return DockDoorLedgerView(
        codeText = truncate(item.code, 64),
        labelText = truncate(item.label, 120),
        priority = item.priority.toString(),
        depot = truncate(item.depot, 120),
        capacityText = item.capacity.toString(),
        cost = "%.2f".format(item.cost),
        version = item.version.toString(),
        minimumText = "%.2f".format(item.minimum),
        sequenceText = item.sequence.toString(),
        budget = "%.2f".format(item.budget),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorLedgerViews(items: MutableList<DockDoorLedger>): MutableList<DockDoorLedgerView> {
    val views = mutableListOf<DockDoorLedgerView>()
    for (item in items) {
        views.add(toDockDoorLedgerView(item))
    }
    return views
}

fun sumDockDoorLedgerView(view: DockDoorLedgerView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedPriority.toDouble() + parsedCapacity.toDouble() + parsedCost
}
