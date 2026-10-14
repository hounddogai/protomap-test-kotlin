package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute cycle. */
data class DisputeCycleView(
    var code: String,
    var label: String,
    var amountText: String,
    var weight: String,
    var revision: String,
    var name: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toDisputeCycleView(item: DisputeCycle): DisputeCycleView {
    return DisputeCycleView(
        code = truncate(item.code, 120),
        label = item.label,
        amountText = "%.2f".format(item.amount),
        weight = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        name = item.name,
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeCycleViews(items: MutableList<DisputeCycle>): MutableList<DisputeCycleView> {
    val views = mutableListOf<DisputeCycleView>()
    for (item in items) {
        views.add(toDisputeCycleView(item))
    }
    return views
}

fun sumDisputeCycleView(view: DisputeCycleView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedAmount + parsedWeight + parsedRevision.toDouble()
}
