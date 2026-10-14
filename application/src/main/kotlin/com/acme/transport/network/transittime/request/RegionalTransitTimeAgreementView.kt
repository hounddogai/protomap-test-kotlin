package com.acme.transport.network.transittime.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional transit time agreement. */
data class RegionalTransitTimeAgreementView(
    var code: String,
    var nameText: String,
    var trailerId: String,
    var margin: String,
    var visibleText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toRegionalTransitTimeAgreementView(item: RegionalTransitTimeAgreement): RegionalTransitTimeAgreementView {
    return RegionalTransitTimeAgreementView(
        code = truncate(item.code, 64),
        nameText = item.name,
        trailerId = item.trailerId,
        margin = "%.2f".format(item.margin),
        visibleText = "${item.visible}",
        reviewer = truncate(item.reviewer, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTransitTimeAgreementViews(items: MutableList<RegionalTransitTimeAgreement>): MutableList<RegionalTransitTimeAgreementView> {
    val views = mutableListOf<RegionalTransitTimeAgreementView>()
    for (item in items) {
        views.add(toRegionalTransitTimeAgreementView(item))
    }
    return views
}

fun sumRegionalTransitTimeAgreementView(view: RegionalTransitTimeAgreementView): Double {
    val parsedMargin = view.margin.toDouble()
    return parsedMargin
}
