package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive capacity proposal. */
data class AdaptiveCapacityProposalView(
    var code: String,
    var name: String,
    var notes: String,
    var phoneText: String,
    var region: String,
    var amount: String,
    var statusLabel: String,
)

fun toAdaptiveCapacityProposalView(item: AdaptiveCapacityProposal): AdaptiveCapacityProposalView {
    return AdaptiveCapacityProposalView(
        code = item.code,
        name = truncate(item.name, 80),
        notes = item.notes,
        phoneText = truncate(item.phone, 64),
        region = item.region,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCapacityProposalViews(items: MutableList<AdaptiveCapacityProposal>): MutableList<AdaptiveCapacityProposalView> {
    val views = mutableListOf<AdaptiveCapacityProposalView>()
    for (item in items) {
        views.add(toAdaptiveCapacityProposalView(item))
    }
    return views
}

fun sumAdaptiveCapacityProposalView(view: AdaptiveCapacityProposalView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
