package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection notice. */
data class InspectionNoticeView(
    var code: String,
    var nameText: String,
    var unit: String,
    var vehicleId: String,
    var depotText: String,
    var score: String,
    var budget: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toInspectionNoticeView(item: InspectionNotice): InspectionNoticeView {
    return InspectionNoticeView(
        code = item.code,
        nameText = item.name,
        unit = item.unit,
        vehicleId = item.vehicleId,
        depotText = item.depot,
        score = "%.2f".format(item.score),
        budget = "%.2f".format(item.budget),
        routeCodeText = truncate(item.routeCode, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionNoticeViews(items: MutableList<InspectionNotice>): MutableList<InspectionNoticeView> {
    val views = mutableListOf<InspectionNoticeView>()
    for (item in items) {
        views.add(toInspectionNoticeView(item))
    }
    return views
}

fun sumInspectionNoticeView(view: InspectionNoticeView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedBudget
}
