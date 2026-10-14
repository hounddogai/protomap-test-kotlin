package com.acme.transport.safety.violation.ledger

/** How screens and exports show a violation proposal. */
data class ViolationProposalView(
    var code: String,
    var title: String,
    var phone: String,
    var region: String,
    var expiresAtText: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toViolationProposalView(item: ViolationProposal): ViolationProposalView {
    return ViolationProposalView(
        code = item.code,
        title = item.title,
        phone = item.phone,
        region = item.region,
        expiresAtText = item.expiresAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationProposalViews(items: MutableList<ViolationProposal>): MutableList<ViolationProposalView> {
    val views = mutableListOf<ViolationProposalView>()
    for (item in items) {
        views.add(toViolationProposalView(item))
    }
    return views
}

fun sumViolationProposalView(view: ViolationProposalView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble()
}
