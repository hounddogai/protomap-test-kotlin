package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation proposal. */
data class SortationProposalView(
    var code: String,
    var title: String,
    var trailerId: String,
    var contactEmailText: String,
    var retries: String,
    var attempts: String,
    var vehicleId: String,
    var weightText: String,
    var statusLabel: String,
)

fun toSortationProposalView(item: SortationProposal): SortationProposalView {
    return SortationProposalView(
        code = truncate(item.code, 80),
        title = item.title,
        trailerId = item.trailerId,
        contactEmailText = item.contactEmail,
        retries = item.retries.toString(),
        attempts = item.attempts.toString(),
        vehicleId = item.vehicleId,
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationProposalViews(items: MutableList<SortationProposal>): MutableList<SortationProposalView> {
    val views = mutableListOf<SortationProposalView>()
    for (item in items) {
        views.add(toSortationProposalView(item))
    }
    return views
}

fun sumSortationProposalView(view: SortationProposalView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedRetries.toDouble() + parsedAttempts.toDouble() + parsedWeight
}
