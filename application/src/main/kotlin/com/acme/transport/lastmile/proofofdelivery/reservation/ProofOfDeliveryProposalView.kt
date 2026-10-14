package com.acme.transport.lastmile.proofofdelivery.reservation

/** How screens and exports show a proof of delivery proposal. */
data class ProofOfDeliveryProposalView(
    var code: String,
    var title: String,
    var startsAt: String,
    var amountText: String,
    var limit: String,
    var durationMinutesText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toProofOfDeliveryProposalView(item: ProofOfDeliveryProposal): ProofOfDeliveryProposalView {
    return ProofOfDeliveryProposalView(
        code = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        amountText = "%.2f".format(item.amount),
        limit = item.limit.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryProposalViews(items: MutableList<ProofOfDeliveryProposal>): MutableList<ProofOfDeliveryProposalView> {
    val views = mutableListOf<ProofOfDeliveryProposalView>()
    for (item in items) {
        views.add(toProofOfDeliveryProposalView(item))
    }
    return views
}

fun sumProofOfDeliveryProposalView(view: ProofOfDeliveryProposalView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedStartsAt.toDouble() + parsedAmount + parsedLimit.toDouble()
}
