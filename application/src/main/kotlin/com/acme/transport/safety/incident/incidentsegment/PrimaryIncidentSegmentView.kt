package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary incident segment. */
data class PrimaryIncidentSegmentView(
    var code: String,
    var titleText: String,
    var visibleText: String,
    var dock: String,
    var amount: String,
    var statusLabel: String,
)

fun toPrimaryIncidentSegmentView(item: PrimaryIncidentSegment): PrimaryIncidentSegmentView {
    return PrimaryIncidentSegmentView(
        code = item.code,
        titleText = item.title,
        visibleText = "${item.visible}",
        dock = truncate(item.dock, 40),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryIncidentSegmentViews(items: MutableList<PrimaryIncidentSegment>): MutableList<PrimaryIncidentSegmentView> {
    val views = mutableListOf<PrimaryIncidentSegmentView>()
    for (item in items) {
        views.add(toPrimaryIncidentSegmentView(item))
    }
    return views
}

fun sumPrimaryIncidentSegmentView(view: PrimaryIncidentSegmentView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
