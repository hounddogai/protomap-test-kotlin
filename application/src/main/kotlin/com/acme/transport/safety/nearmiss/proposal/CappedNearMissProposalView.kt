package com.acme.transport.safety.nearmiss.proposal

/** How screens and exports show a capped near miss proposal. */
data class CappedNearMissProposalView(
    var code: String,
    var label: String,
    var limit: String,
    var version: String,
    var channelText: String,
    var depot: String,
    var statusLabel: String,
)

fun toCappedNearMissProposalView(item: CappedNearMissProposal): CappedNearMissProposalView {
    return CappedNearMissProposalView(
        code = item.code,
        label = item.label,
        limit = item.limit.toString(),
        version = item.version.toString(),
        channelText = item.channel,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedNearMissProposalViews(items: MutableList<CappedNearMissProposal>): MutableList<CappedNearMissProposalView> {
    val views = mutableListOf<CappedNearMissProposalView>()
    for (item in items) {
        views.add(toCappedNearMissProposalView(item))
    }
    return views
}

fun sumCappedNearMissProposalView(view: CappedNearMissProposalView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedVersion = view.version.toInt()
    return parsedLimit.toDouble() + parsedVersion.toDouble()
}
