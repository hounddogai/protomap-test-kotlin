package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery batch. */
data class RedeliveryBatchView(
    var code: String,
    var name: String,
    var budget: String,
    var routeCode: String,
    var updatedAt: String,
    var amountText: String,
    var contactEmailText: String,
    var archived: String,
    var pallets: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toRedeliveryBatchView(item: RedeliveryBatch): RedeliveryBatchView {
    return RedeliveryBatchView(
        code = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        routeCode = truncate(item.routeCode, 64),
        updatedAt = item.updatedAt.toString(),
        amountText = "%.2f".format(item.amount),
        contactEmailText = item.contactEmail,
        archived = "${item.archived}",
        pallets = item.pallets.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryBatchViews(items: MutableList<RedeliveryBatch>): MutableList<RedeliveryBatchView> {
    val views = mutableListOf<RedeliveryBatchView>()
    for (item in items) {
        views.add(toRedeliveryBatchView(item))
    }
    return views
}

fun sumRedeliveryBatchView(view: RedeliveryBatchView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedBudget + parsedUpdatedAt.toDouble() + parsedAmount
}
