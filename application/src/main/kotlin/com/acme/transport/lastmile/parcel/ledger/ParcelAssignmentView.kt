package com.acme.transport.lastmile.parcel.ledger

/** How screens and exports show a parcel assignment. */
data class ParcelAssignmentView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var durationMinutes: String,
    var phone: String,
    var startsAt: String,
    var priority: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toParcelAssignmentView(item: ParcelAssignment): ParcelAssignmentView {
    return ParcelAssignmentView(
        code = item.code,
        title = item.title,
        pinnedText = "${item.pinned}",
        durationMinutes = item.durationMinutes.toString(),
        phone = item.phone,
        startsAt = item.startsAt.toString(),
        priority = item.priority.toString(),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelAssignmentViews(items: MutableList<ParcelAssignment>): MutableList<ParcelAssignmentView> {
    val views = mutableListOf<ParcelAssignmentView>()
    for (item in items) {
        views.add(toParcelAssignmentView(item))
    }
    return views
}

fun sumParcelAssignmentView(view: ParcelAssignmentView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDurationMinutes.toDouble() + parsedStartsAt.toDouble() + parsedPriority.toDouble()
}
