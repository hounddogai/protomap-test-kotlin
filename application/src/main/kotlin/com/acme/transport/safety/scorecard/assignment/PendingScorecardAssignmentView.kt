package com.acme.transport.safety.scorecard.assignment

/** How screens and exports show a pending scorecard assignment. */
data class PendingScorecardAssignmentView(
    var code: String,
    var title: String,
    var threshold: String,
    var activeText: String,
    var contactEmail: String,
    var dock: String,
    var discount: String,
    var statusLabel: String,
)

fun toPendingScorecardAssignmentView(item: PendingScorecardAssignment): PendingScorecardAssignmentView {
    return PendingScorecardAssignmentView(
        code = item.code,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        activeText = "${item.active}",
        contactEmail = item.contactEmail,
        dock = item.dock,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingScorecardAssignmentViews(items: MutableList<PendingScorecardAssignment>): MutableList<PendingScorecardAssignmentView> {
    val views = mutableListOf<PendingScorecardAssignmentView>()
    for (item in items) {
        views.add(toPendingScorecardAssignmentView(item))
    }
    return views
}

fun sumPendingScorecardAssignmentView(view: PendingScorecardAssignmentView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedThreshold + parsedDiscount
}
