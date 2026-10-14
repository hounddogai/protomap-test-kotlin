package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period proposal. */
data class DutyPeriodProposalView(
    var code: String,
    var titleText: String,
    var stopCount: String,
    var amount: String,
    var categoryText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDutyPeriodProposalView(item: DutyPeriodProposal): DutyPeriodProposalView {
    return DutyPeriodProposalView(
        code = item.code,
        titleText = item.title,
        stopCount = item.stopCount.toString(),
        amount = "%.2f".format(item.amount),
        categoryText = truncate(item.category, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodProposalViews(items: MutableList<DutyPeriodProposal>): MutableList<DutyPeriodProposalView> {
    val views = mutableListOf<DutyPeriodProposalView>()
    for (item in items) {
        views.add(toDutyPeriodProposalView(item))
    }
    return views
}

fun sumDutyPeriodProposalView(view: DutyPeriodProposalView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedStopCount.toDouble() + parsedAmount + parsedQuantity.toDouble()
}
