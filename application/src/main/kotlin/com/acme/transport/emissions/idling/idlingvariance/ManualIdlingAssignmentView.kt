package com.acme.transport.emissions.idling.idlingvariance

/** How screens and exports show a manual idling assignment. */
data class ManualIdlingAssignmentView(
    var code: String,
    var title: String,
    var attempts: String,
    var lane: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toManualIdlingAssignmentView(item: ManualIdlingAssignment): ManualIdlingAssignmentView {
    return ManualIdlingAssignmentView(
        code = item.code,
        title = item.title,
        attempts = item.attempts.toString(),
        lane = item.lane,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualIdlingAssignmentViews(items: MutableList<ManualIdlingAssignment>): MutableList<ManualIdlingAssignmentView> {
    val views = mutableListOf<ManualIdlingAssignmentView>()
    for (item in items) {
        views.add(toManualIdlingAssignmentView(item))
    }
    return views
}

fun sumManualIdlingAssignmentView(view: ManualIdlingAssignmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedAttempts.toDouble() + parsedLoadKg
}
