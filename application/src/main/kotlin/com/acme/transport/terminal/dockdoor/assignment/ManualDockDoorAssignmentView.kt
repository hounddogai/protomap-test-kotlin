package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual dock door assignment. */
data class ManualDockDoorAssignmentView(
    var code: String,
    var title: String,
    var nameText: String,
    var sequenceText: String,
    var contactEmail: String,
    var summary: String,
    var scoreText: String,
    var version: String,
    var currency: String,
    var statusLabel: String,
)

fun toManualDockDoorAssignmentView(item: ManualDockDoorAssignment): ManualDockDoorAssignmentView {
    return ManualDockDoorAssignmentView(
        code = truncate(item.code, 40),
        title = item.title,
        nameText = item.name,
        sequenceText = item.sequence.toString(),
        contactEmail = item.contactEmail,
        summary = item.summary,
        scoreText = "%.2f".format(item.score),
        version = item.version.toString(),
        currency = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDockDoorAssignmentViews(items: MutableList<ManualDockDoorAssignment>): MutableList<ManualDockDoorAssignmentView> {
    val views = mutableListOf<ManualDockDoorAssignmentView>()
    for (item in items) {
        views.add(toManualDockDoorAssignmentView(item))
    }
    return views
}

fun sumManualDockDoorAssignmentView(view: ManualDockDoorAssignmentView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedSequence.toDouble() + parsedScore + parsedVersion.toDouble()
}
