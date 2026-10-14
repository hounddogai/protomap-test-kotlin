package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time proposal. */
data class TransitTimeProposalView(
    var codeText: String,
    var name: String,
    var budget: String,
    var discount: String,
    var externalRef: String,
    var etaMinutes: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toTransitTimeProposalView(item: TransitTimeProposal): TransitTimeProposalView {
    return TransitTimeProposalView(
        codeText = truncate(item.code, 120),
        name = truncate(item.name, 64),
        budget = "%.2f".format(item.budget),
        discount = "%.2f".format(item.discount),
        externalRef = item.externalRef,
        etaMinutes = item.etaMinutes.toString(),
        trailerId = truncate(item.trailerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeProposalViews(items: MutableList<TransitTimeProposal>): MutableList<TransitTimeProposalView> {
    val views = mutableListOf<TransitTimeProposalView>()
    for (item in items) {
        views.add(toTransitTimeProposalView(item))
    }
    return views
}

fun sumTransitTimeProposalView(view: TransitTimeProposalView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedBudget + parsedDiscount + parsedEtaMinutes.toDouble()
}
