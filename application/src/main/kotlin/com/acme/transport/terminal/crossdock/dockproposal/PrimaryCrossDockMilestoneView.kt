package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary cross dock milestone. */
data class PrimaryCrossDockMilestoneView(
    var code: String,
    var name: String,
    var reviewer: String,
    var externalRef: String,
    var currency: String,
    var routeCode: String,
    var summary: String,
    var versionText: String,
    var statusLabel: String,
)

fun toPrimaryCrossDockMilestoneView(item: PrimaryCrossDockMilestone): PrimaryCrossDockMilestoneView {
    return PrimaryCrossDockMilestoneView(
        code = item.code,
        name = item.name,
        reviewer = item.reviewer,
        externalRef = item.externalRef,
        currency = item.currency,
        routeCode = truncate(item.routeCode, 80),
        summary = item.summary,
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCrossDockMilestoneViews(items: MutableList<PrimaryCrossDockMilestone>): MutableList<PrimaryCrossDockMilestoneView> {
    val views = mutableListOf<PrimaryCrossDockMilestoneView>()
    for (item in items) {
        views.add(toPrimaryCrossDockMilestoneView(item))
    }
    return views
}

fun sumPrimaryCrossDockMilestoneView(view: PrimaryCrossDockMilestoneView): Double {
    val parsedVersion = view.versionText.toInt()
    return parsedVersion.toDouble()
}
