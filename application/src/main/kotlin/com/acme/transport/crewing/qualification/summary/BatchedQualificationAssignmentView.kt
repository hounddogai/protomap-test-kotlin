package com.acme.transport.crewing.qualification.summary

/** How screens and exports show a batched qualification assignment. */
data class BatchedQualificationAssignmentView(
    var code: String,
    var nameText: String,
    var expiresAt: String,
    var timezone: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toBatchedQualificationAssignmentView(item: BatchedQualificationAssignment): BatchedQualificationAssignmentView {
    return BatchedQualificationAssignmentView(
        code = item.code,
        nameText = item.name,
        expiresAt = item.expiresAt.toString(),
        timezone = item.timezone,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedQualificationAssignmentViews(items: MutableList<BatchedQualificationAssignment>): MutableList<BatchedQualificationAssignmentView> {
    val views = mutableListOf<BatchedQualificationAssignmentView>()
    for (item in items) {
        views.add(toBatchedQualificationAssignmentView(item))
    }
    return views
}

fun sumBatchedQualificationAssignmentView(view: BatchedQualificationAssignmentView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedExpiresAt.toDouble() + parsedDistanceKm
}
