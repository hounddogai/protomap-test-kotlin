package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tender proposal. */
data class AdaptiveTenderProposalView(
    var codeText: String,
    var titleText: String,
    var minimum: String,
    var checksum: String,
    var channel: String,
    var etaMinutes: String,
    var contactEmail: String,
    var budget: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toAdaptiveTenderProposalView(item: AdaptiveTenderProposal): AdaptiveTenderProposalView {
    return AdaptiveTenderProposalView(
        codeText = item.code,
        titleText = item.title,
        minimum = "%.2f".format(item.minimum),
        checksum = item.checksum,
        channel = truncate(item.channel, 40),
        etaMinutes = item.etaMinutes.toString(),
        contactEmail = item.contactEmail,
        budget = "%.2f".format(item.budget),
        trailerId = truncate(item.trailerId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTenderProposalViews(items: MutableList<AdaptiveTenderProposal>): MutableList<AdaptiveTenderProposalView> {
    val views = mutableListOf<AdaptiveTenderProposalView>()
    for (item in items) {
        views.add(toAdaptiveTenderProposalView(item))
    }
    return views
}

fun sumAdaptiveTenderProposalView(view: AdaptiveTenderProposalView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedMinimum + parsedEtaMinutes.toDouble() + parsedBudget
}
