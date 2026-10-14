package com.acme.transport.crewing.qualification.plan

/** How screens and exports show a qualification proposal. */
data class QualificationProposalView(
    var code: String,
    var name: String,
    var reviewerText: String,
    var lane: String,
    var limit: String,
    var shiftCode: String,
    var driverId: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toQualificationProposalView(item: QualificationProposal): QualificationProposalView {
    return QualificationProposalView(
        code = item.code,
        name = item.name,
        reviewerText = item.reviewer,
        lane = item.lane,
        limit = item.limit.toString(),
        shiftCode = item.shiftCode,
        driverId = item.driverId,
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationProposalViews(items: MutableList<QualificationProposal>): MutableList<QualificationProposalView> {
    val views = mutableListOf<QualificationProposalView>()
    for (item in items) {
        views.add(toQualificationProposalView(item))
    }
    return views
}

fun sumQualificationProposalView(view: QualificationProposalView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedLimit.toDouble() + parsedAttempts.toDouble()
}
