package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection assignment. */
data class InspectionAssignmentView(
    var code: String,
    var labelText: String,
    var externalRef: String,
    var etaMinutes: String,
    var score: String,
    var country: String,
    var ratio: String,
    var statusLabel: String,
)

fun toInspectionAssignmentView(item: InspectionAssignment): InspectionAssignmentView {
    return InspectionAssignmentView(
        code = item.code,
        labelText = item.label,
        externalRef = truncate(item.externalRef, 120),
        etaMinutes = item.etaMinutes.toString(),
        score = "%.2f".format(item.score),
        country = truncate(item.country, 40),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionAssignmentViews(items: MutableList<InspectionAssignment>): MutableList<InspectionAssignmentView> {
    val views = mutableListOf<InspectionAssignmentView>()
    for (item in items) {
        views.add(toInspectionAssignmentView(item))
    }
    return views
}

fun sumInspectionAssignmentView(view: InspectionAssignmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedScore = view.score.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedEtaMinutes.toDouble() + parsedScore + parsedRatio
}
