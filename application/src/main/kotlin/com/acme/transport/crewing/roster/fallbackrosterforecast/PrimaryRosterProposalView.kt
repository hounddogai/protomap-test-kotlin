package com.acme.transport.crewing.roster.fallbackrosterforecast

/** How screens and exports show a primary roster proposal. */
data class PrimaryRosterProposalView(
    var code: String,
    var name: String,
    var version: String,
    var laneText: String,
    var category: String,
    var localeText: String,
    var statusLabel: String,
)

fun toPrimaryRosterProposalView(item: PrimaryRosterProposal): PrimaryRosterProposalView {
    return PrimaryRosterProposalView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        laneText = item.lane,
        category = item.category,
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRosterProposalViews(items: MutableList<PrimaryRosterProposal>): MutableList<PrimaryRosterProposalView> {
    val views = mutableListOf<PrimaryRosterProposalView>()
    for (item in items) {
        views.add(toPrimaryRosterProposalView(item))
    }
    return views
}

fun sumPrimaryRosterProposalView(view: PrimaryRosterProposalView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
