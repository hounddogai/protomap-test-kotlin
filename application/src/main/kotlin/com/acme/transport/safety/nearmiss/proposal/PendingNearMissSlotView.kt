package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending near miss slot. */
data class PendingNearMissSlotView(
    var code: String,
    var nameText: String,
    var etaMinutes: String,
    var unit: String,
    var budgetText: String,
    var dockText: String,
    var priority: String,
    var pinnedText: String,
    var title: String,
    var statusLabel: String,
)

fun toPendingNearMissSlotView(item: PendingNearMissSlot): PendingNearMissSlotView {
    return PendingNearMissSlotView(
        code = item.code,
        nameText = item.name,
        etaMinutes = item.etaMinutes.toString(),
        unit = item.unit,
        budgetText = "%.2f".format(item.budget),
        dockText = item.dock,
        priority = item.priority.toString(),
        pinnedText = "${item.pinned}",
        title = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNearMissSlotViews(items: MutableList<PendingNearMissSlot>): MutableList<PendingNearMissSlotView> {
    val views = mutableListOf<PendingNearMissSlotView>()
    for (item in items) {
        views.add(toPendingNearMissSlotView(item))
    }
    return views
}

fun sumPendingNearMissSlotView(view: PendingNearMissSlotView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedEtaMinutes.toDouble() + parsedBudget + parsedPriority.toDouble()
}
