package com.acme.transport.lastmile.attempt.attemptplan

/** How screens and exports show a attempt assignment. */
data class AttemptAssignmentView(
    var code: String,
    var titleText: String,
    var descriptionText: String,
    var startsAt: String,
    var ownerId: String,
    var loadKg: String,
    var region: String,
    var statusLabel: String,
)

fun toAttemptAssignmentView(item: AttemptAssignment): AttemptAssignmentView {
    return AttemptAssignmentView(
        code = item.code,
        titleText = item.title,
        descriptionText = item.description,
        startsAt = item.startsAt.toString(),
        ownerId = item.ownerId,
        loadKg = "%.2f".format(item.loadKg),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptAssignmentViews(items: MutableList<AttemptAssignment>): MutableList<AttemptAssignmentView> {
    val views = mutableListOf<AttemptAssignmentView>()
    for (item in items) {
        views.add(toAttemptAssignmentView(item))
    }
    return views
}

fun sumAttemptAssignmentView(view: AttemptAssignmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedStartsAt.toDouble() + parsedLoadKg
}
