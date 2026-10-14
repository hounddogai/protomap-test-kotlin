package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict audit proposal. */
data class StrictAuditProposalView(
    var code: String,
    var title: String,
    var budgetText: String,
    var depot: String,
    var updatedAt: String,
    var limitText: String,
    var dock: String,
    var externalRef: String,
    var channelText: String,
    var statusLabel: String,
)

fun toStrictAuditProposalView(item: StrictAuditProposal): StrictAuditProposalView {
    return StrictAuditProposalView(
        code = item.code,
        title = item.title,
        budgetText = "%.2f".format(item.budget),
        depot = truncate(item.depot, 64),
        updatedAt = item.updatedAt.toString(),
        limitText = item.limit.toString(),
        dock = item.dock,
        externalRef = item.externalRef,
        channelText = truncate(item.channel, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAuditProposalViews(items: MutableList<StrictAuditProposal>): MutableList<StrictAuditProposalView> {
    val views = mutableListOf<StrictAuditProposalView>()
    for (item in items) {
        views.add(toStrictAuditProposalView(item))
    }
    return views
}

fun sumStrictAuditProposalView(view: StrictAuditProposalView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedBudget + parsedUpdatedAt.toDouble() + parsedLimit.toDouble()
}
