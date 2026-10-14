package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive relay assignment. */
data class AdaptiveRelayAssignmentView(
    var codeText: String,
    var titleText: String,
    var routeCode: String,
    var contactEmail: String,
    var depot: String,
    var source: String,
    var pinned: String,
    var visibleText: String,
    var loadKg: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toAdaptiveRelayAssignmentView(item: AdaptiveRelayAssignment): AdaptiveRelayAssignmentView {
    return AdaptiveRelayAssignmentView(
        codeText = item.code,
        titleText = item.title,
        routeCode = truncate(item.routeCode, 64),
        contactEmail = truncate(item.contactEmail, 80),
        depot = truncate(item.depot, 64),
        source = item.source,
        pinned = "${item.pinned}",
        visibleText = "${item.visible}",
        loadKg = "%.2f".format(item.loadKg),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRelayAssignmentViews(items: MutableList<AdaptiveRelayAssignment>): MutableList<AdaptiveRelayAssignmentView> {
    val views = mutableListOf<AdaptiveRelayAssignmentView>()
    for (item in items) {
        views.add(toAdaptiveRelayAssignmentView(item))
    }
    return views
}

fun sumAdaptiveRelayAssignmentView(view: AdaptiveRelayAssignmentView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLoadKg
}
