package com.acme.transport.network.node.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling node proposal. */
data class RollingNodeProposalView(
    var code: String,
    var title: String,
    var locale: String,
    var ceiling: String,
    var routeCodeText: String,
    var ratio: String,
    var budget: String,
    var windowDaysText: String,
    var trailerIdText: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toRollingNodeProposalView(item: RollingNodeProposal): RollingNodeProposalView {
    return RollingNodeProposalView(
        code = item.code,
        title = item.title,
        locale = item.locale,
        ceiling = "%.2f".format(item.ceiling),
        routeCodeText = item.routeCode,
        ratio = "%.2f".format(item.ratio),
        budget = "%.2f".format(item.budget),
        windowDaysText = item.windowDays.toString(),
        trailerIdText = item.trailerId,
        vehicleIdText = truncate(item.vehicleId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingNodeProposalViews(items: MutableList<RollingNodeProposal>): MutableList<RollingNodeProposalView> {
    val views = mutableListOf<RollingNodeProposalView>()
    for (item in items) {
        views.add(toRollingNodeProposalView(item))
    }
    return views
}

fun sumRollingNodeProposalView(view: RollingNodeProposalView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedCeiling + parsedRatio + parsedBudget
}
