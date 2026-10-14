package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection baseline. */
data class InspectionBaselineView(
    var codeText: String,
    var label: String,
    var costText: String,
    var contactEmailText: String,
    var version: String,
    var endsAt: String,
    var scoreText: String,
    var title: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toInspectionBaselineView(item: InspectionBaseline): InspectionBaselineView {
    return InspectionBaselineView(
        codeText = item.code,
        label = item.label,
        costText = "%.2f".format(item.cost),
        contactEmailText = item.contactEmail,
        version = item.version.toString(),
        endsAt = item.endsAt.toString(),
        scoreText = "%.2f".format(item.score),
        title = truncate(item.title, 40),
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionBaselineViews(items: MutableList<InspectionBaseline>): MutableList<InspectionBaselineView> {
    val views = mutableListOf<InspectionBaselineView>()
    for (item in items) {
        views.add(toInspectionBaselineView(item))
    }
    return views
}

fun sumInspectionBaselineView(view: InspectionBaselineView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedCost + parsedVersion.toDouble() + parsedEndsAt.toDouble()
}
