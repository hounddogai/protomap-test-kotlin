package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection rule. */
data class InspectionRuleView(
    var codeText: String,
    var label: String,
    var trailerId: String,
    var externalRefText: String,
    var summary: String,
    var score: String,
    var statusLabel: String,
)

fun toInspectionRuleView(item: InspectionRule): InspectionRuleView {
    return InspectionRuleView(
        codeText = item.code,
        label = truncate(item.label, 40),
        trailerId = truncate(item.trailerId, 40),
        externalRefText = truncate(item.externalRef, 64),
        summary = truncate(item.summary, 120),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionRuleViews(items: MutableList<InspectionRule>): MutableList<InspectionRuleView> {
    val views = mutableListOf<InspectionRuleView>()
    for (item in items) {
        views.add(toInspectionRuleView(item))
    }
    return views
}

fun sumInspectionRuleView(view: InspectionRuleView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
