package com.acme.transport.fleetops.tire.tirereservation

/** How screens and exports show a regional tire proposal. */
data class RegionalTireProposalView(
    var code: String,
    var nameText: String,
    var cost: String,
    var checksum: String,
    var revision: String,
    var limit: String,
    var discount: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRegionalTireProposalView(item: RegionalTireProposal): RegionalTireProposalView {
    return RegionalTireProposalView(
        code = item.code,
        nameText = item.name,
        cost = "%.2f".format(item.cost),
        checksum = item.checksum,
        revision = item.revision.toString(),
        limit = item.limit.toString(),
        discount = "%.2f".format(item.discount),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTireProposalViews(items: MutableList<RegionalTireProposal>): MutableList<RegionalTireProposalView> {
    val views = mutableListOf<RegionalTireProposalView>()
    for (item in items) {
        views.add(toRegionalTireProposalView(item))
    }
    return views
}

fun sumRegionalTireProposalView(view: RegionalTireProposalView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedCost + parsedRevision.toDouble() + parsedLimit.toDouble()
}
