package com.acme.transport.safety.violation.proposal

/** How screens and exports show a rolling violation proposal. */
data class RollingViolationProposalView(
    var codeText: String,
    var label: String,
    var etaMinutes: String,
    var distanceKm: String,
    var amount: String,
    var rateText: String,
    var contactEmail: String,
    var category: String,
    var statusLabel: String,
)

fun toRollingViolationProposalView(item: RollingViolationProposal): RollingViolationProposalView {
    return RollingViolationProposalView(
        codeText = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        amount = "%.2f".format(item.amount),
        rateText = "%.2f".format(item.rate),
        contactEmail = item.contactEmail,
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingViolationProposalViews(items: MutableList<RollingViolationProposal>): MutableList<RollingViolationProposalView> {
    val views = mutableListOf<RollingViolationProposalView>()
    for (item in items) {
        views.add(toRollingViolationProposalView(item))
    }
    return views
}

fun sumRollingViolationProposalView(view: RollingViolationProposalView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedEtaMinutes.toDouble() + parsedDistanceKm + parsedAmount
}
