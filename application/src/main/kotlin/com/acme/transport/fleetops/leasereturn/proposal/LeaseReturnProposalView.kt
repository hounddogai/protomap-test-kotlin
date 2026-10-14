package com.acme.transport.fleetops.leasereturn.proposal

/** How screens and exports show a lease return proposal. */
data class LeaseReturnProposalView(
    var code: String,
    var label: String,
    var createdAt: String,
    var region: String,
    var activeText: String,
    var limitText: String,
    var statusLabel: String,
)

fun toLeaseReturnProposalView(item: LeaseReturnProposal): LeaseReturnProposalView {
    return LeaseReturnProposalView(
        code = item.code,
        label = item.label,
        createdAt = item.createdAt.toString(),
        region = item.region,
        activeText = "${item.active}",
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnProposalViews(items: MutableList<LeaseReturnProposal>): MutableList<LeaseReturnProposalView> {
    val views = mutableListOf<LeaseReturnProposalView>()
    for (item in items) {
        views.add(toLeaseReturnProposalView(item))
    }
    return views
}

fun sumLeaseReturnProposalView(view: LeaseReturnProposalView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedCreatedAt.toDouble() + parsedLimit.toDouble()
}
