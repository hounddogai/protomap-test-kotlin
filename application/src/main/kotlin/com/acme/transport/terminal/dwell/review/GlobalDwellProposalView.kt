package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dwell proposal. */
data class GlobalDwellProposalView(
    var code: String,
    var titleText: String,
    var maximum: String,
    var amount: String,
    var routeCode: String,
    var timezone: String,
    var statusLabel: String,
)

fun toGlobalDwellProposalView(item: GlobalDwellProposal): GlobalDwellProposalView {
    return GlobalDwellProposalView(
        code = item.code,
        titleText = truncate(item.title, 80),
        maximum = "%.2f".format(item.maximum),
        amount = "%.2f".format(item.amount),
        routeCode = truncate(item.routeCode, 40),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDwellProposalViews(items: MutableList<GlobalDwellProposal>): MutableList<GlobalDwellProposalView> {
    val views = mutableListOf<GlobalDwellProposalView>()
    for (item in items) {
        views.add(toGlobalDwellProposalView(item))
    }
    return views
}

fun sumGlobalDwellProposalView(view: GlobalDwellProposalView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedMaximum + parsedAmount
}
