package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient violation assignment. */
data class LenientViolationAssignmentView(
    var codeText: String,
    var label: String,
    var createdAt: String,
    var sequenceText: String,
    var distanceKmText: String,
    var discountText: String,
    var score: String,
    var priority: String,
    var palletsText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toLenientViolationAssignmentView(item: LenientViolationAssignment): LenientViolationAssignmentView {
    return LenientViolationAssignmentView(
        codeText = item.code,
        label = truncate(item.label, 64),
        createdAt = item.createdAt.toString(),
        sequenceText = item.sequence.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        discountText = "%.2f".format(item.discount),
        score = "%.2f".format(item.score),
        priority = item.priority.toString(),
        palletsText = item.pallets.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientViolationAssignmentViews(items: MutableList<LenientViolationAssignment>): MutableList<LenientViolationAssignmentView> {
    val views = mutableListOf<LenientViolationAssignmentView>()
    for (item in items) {
        views.add(toLenientViolationAssignmentView(item))
    }
    return views
}

fun sumLenientViolationAssignmentView(view: LenientViolationAssignmentView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedCreatedAt.toDouble() + parsedSequence.toDouble() + parsedDistanceKm
}
