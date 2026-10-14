package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending attempt entry. */
data class PendingAttemptEntryView(
    var code: String,
    var nameText: String,
    var budgetText: String,
    var shiftCodeText: String,
    var titleText: String,
    var floorValue: String,
    var label: String,
    var trailerId: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toPendingAttemptEntryView(item: PendingAttemptEntry): PendingAttemptEntryView {
    return PendingAttemptEntryView(
        code = item.code,
        nameText = item.name,
        budgetText = "%.2f".format(item.budget),
        shiftCodeText = item.shiftCode,
        titleText = truncate(item.title, 40),
        floorValue = "%.2f".format(item.floorValue),
        label = truncate(item.label, 64),
        trailerId = item.trailerId,
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAttemptEntryViews(items: MutableList<PendingAttemptEntry>): MutableList<PendingAttemptEntryView> {
    val views = mutableListOf<PendingAttemptEntryView>()
    for (item in items) {
        views.add(toPendingAttemptEntryView(item))
    }
    return views
}

fun sumPendingAttemptEntryView(view: PendingAttemptEntryView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedBudget + parsedFloorValue
}
