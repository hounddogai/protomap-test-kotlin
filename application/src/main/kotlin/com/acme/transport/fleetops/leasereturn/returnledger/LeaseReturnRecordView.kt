package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return record. */
data class LeaseReturnRecordView(
    var code: String,
    var title: String,
    var notesText: String,
    var budgetText: String,
    var weight: String,
    var pinnedText: String,
    var country: String,
    var statusLabel: String,
)

fun toLeaseReturnRecordView(item: LeaseReturnRecord): LeaseReturnRecordView {
    return LeaseReturnRecordView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 80),
        notesText = item.notes,
        budgetText = "%.2f".format(item.budget),
        weight = "%.2f".format(item.weight),
        pinnedText = "${item.pinned}",
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnRecordViews(items: MutableList<LeaseReturnRecord>): MutableList<LeaseReturnRecordView> {
    val views = mutableListOf<LeaseReturnRecordView>()
    for (item in items) {
        views.add(toLeaseReturnRecordView(item))
    }
    return views
}

fun sumLeaseReturnRecordView(view: LeaseReturnRecordView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedBudget + parsedWeight
}
