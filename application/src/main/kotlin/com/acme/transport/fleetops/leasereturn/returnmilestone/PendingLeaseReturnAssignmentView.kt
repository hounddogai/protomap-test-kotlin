package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending lease return assignment. */
data class PendingLeaseReturnAssignmentView(
    var codeText: String,
    var titleText: String,
    var source: String,
    var score: String,
    var description: String,
    var routeCodeText: String,
    var distanceKmText: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toPendingLeaseReturnAssignmentView(item: PendingLeaseReturnAssignment): PendingLeaseReturnAssignmentView {
    return PendingLeaseReturnAssignmentView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        source = item.source,
        score = "%.2f".format(item.score),
        description = item.description,
        routeCodeText = truncate(item.routeCode, 40),
        distanceKmText = "%.2f".format(item.distanceKm),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLeaseReturnAssignmentViews(items: MutableList<PendingLeaseReturnAssignment>): MutableList<PendingLeaseReturnAssignmentView> {
    val views = mutableListOf<PendingLeaseReturnAssignmentView>()
    for (item in items) {
        views.add(toPendingLeaseReturnAssignmentView(item))
    }
    return views
}

fun sumPendingLeaseReturnAssignmentView(view: PendingLeaseReturnAssignmentView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedScore + parsedDistanceKm + parsedRetries.toDouble()
}
