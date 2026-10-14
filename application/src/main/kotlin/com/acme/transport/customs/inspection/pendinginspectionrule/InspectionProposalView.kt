package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection proposal. */
data class InspectionProposalView(
    var code: String,
    var label: String,
    var shiftCodeText: String,
    var name: String,
    var score: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toInspectionProposalView(item: InspectionProposal): InspectionProposalView {
    return InspectionProposalView(
        code = truncate(item.code, 120),
        label = item.label,
        shiftCodeText = item.shiftCode,
        name = item.name,
        score = "%.2f".format(item.score),
        reviewer = truncate(item.reviewer, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionProposalViews(items: MutableList<InspectionProposal>): MutableList<InspectionProposalView> {
    val views = mutableListOf<InspectionProposalView>()
    for (item in items) {
        views.add(toInspectionProposalView(item))
    }
    return views
}

fun sumInspectionProposalView(view: InspectionProposalView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
