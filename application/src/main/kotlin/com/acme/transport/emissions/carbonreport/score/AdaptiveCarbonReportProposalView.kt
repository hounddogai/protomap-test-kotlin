package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive carbon report proposal. */
data class AdaptiveCarbonReportProposalView(
    var code: String,
    var titleText: String,
    var lane: String,
    var startsAt: String,
    var dockText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toAdaptiveCarbonReportProposalView(item: AdaptiveCarbonReportProposal): AdaptiveCarbonReportProposalView {
    return AdaptiveCarbonReportProposalView(
        code = item.code,
        titleText = truncate(item.title, 80),
        lane = truncate(item.lane, 40),
        startsAt = item.startsAt.toString(),
        dockText = item.dock,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCarbonReportProposalViews(items: MutableList<AdaptiveCarbonReportProposal>): MutableList<AdaptiveCarbonReportProposalView> {
    val views = mutableListOf<AdaptiveCarbonReportProposalView>()
    for (item in items) {
        views.add(toAdaptiveCarbonReportProposalView(item))
    }
    return views
}

fun sumAdaptiveCarbonReportProposalView(view: AdaptiveCarbonReportProposalView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
