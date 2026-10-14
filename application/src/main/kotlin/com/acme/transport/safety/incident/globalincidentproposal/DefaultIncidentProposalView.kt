package com.acme.transport.safety.incident.globalincidentproposal

/** How screens and exports show a default incident proposal. */
data class DefaultIncidentProposalView(
    var code: String,
    var label: String,
    var graceDays: String,
    var attempts: String,
    var pallets: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDefaultIncidentProposalView(item: DefaultIncidentProposal): DefaultIncidentProposalView {
    return DefaultIncidentProposalView(
        code = item.code,
        label = item.label,
        graceDays = item.graceDays.toString(),
        attempts = item.attempts.toString(),
        pallets = item.pallets.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultIncidentProposalViews(items: MutableList<DefaultIncidentProposal>): MutableList<DefaultIncidentProposalView> {
    val views = mutableListOf<DefaultIncidentProposalView>()
    for (item in items) {
        views.add(toDefaultIncidentProposalView(item))
    }
    return views
}

fun sumDefaultIncidentProposalView(view: DefaultIncidentProposalView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedGraceDays.toDouble() + parsedAttempts.toDouble() + parsedPallets.toDouble()
}
