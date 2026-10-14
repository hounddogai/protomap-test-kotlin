package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute slot. */
data class DisputeSlotView(
    var code: String,
    var name: String,
    var externalRef: String,
    var revisionText: String,
    var budgetText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toDisputeSlotView(item: DisputeSlot): DisputeSlotView {
    return DisputeSlotView(
        code = truncate(item.code, 64),
        name = item.name,
        externalRef = item.externalRef,
        revisionText = item.revision.toString(),
        budgetText = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeSlotViews(items: MutableList<DisputeSlot>): MutableList<DisputeSlotView> {
    val views = mutableListOf<DisputeSlotView>()
    for (item in items) {
        views.add(toDisputeSlotView(item))
    }
    return views
}

fun sumDisputeSlotView(view: DisputeSlotView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedRevision.toDouble() + parsedBudget + parsedGraceDays.toDouble()
}
