package com.acme.transport.terminal.sortation.recorditem

/** How screens and exports show a sortation assignment. */
data class SortationAssignmentView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var odometerText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toSortationAssignmentView(item: SortationAssignment): SortationAssignmentView {
    return SortationAssignmentView(
        code = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        odometerText = item.odometer.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationAssignmentViews(items: MutableList<SortationAssignment>): MutableList<SortationAssignmentView> {
    val views = mutableListOf<SortationAssignmentView>()
    for (item in items) {
        views.add(toSortationAssignmentView(item))
    }
    return views
}

fun sumSortationAssignmentView(view: SortationAssignmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedEtaMinutes.toDouble() + parsedOdometer.toDouble() + parsedAttempts.toDouble()
}
