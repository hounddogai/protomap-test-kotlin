package com.acme.transport.lastmile.locker.assignment

/** How screens and exports show a locker assignment. */
data class LockerAssignmentView(
    var code: String,
    var name: String,
    var startsAt: String,
    var sequence: String,
    var attempts: String,
    var category: String,
    var ownerId: String,
    var approvedText: String,
    var archivedText: String,
    var checksum: String,
    var statusLabel: String,
)

fun toLockerAssignmentView(item: LockerAssignment): LockerAssignmentView {
    return LockerAssignmentView(
        code = item.code,
        name = item.name,
        startsAt = item.startsAt.toString(),
        sequence = item.sequence.toString(),
        attempts = item.attempts.toString(),
        category = item.category,
        ownerId = item.ownerId,
        approvedText = "${item.approved}",
        archivedText = "${item.archived}",
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerAssignmentViews(items: MutableList<LockerAssignment>): MutableList<LockerAssignmentView> {
    val views = mutableListOf<LockerAssignmentView>()
    for (item in items) {
        views.add(toLockerAssignmentView(item))
    }
    return views
}

fun sumLockerAssignmentView(view: LockerAssignmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedStartsAt.toDouble() + parsedSequence.toDouble() + parsedAttempts.toDouble()
}
