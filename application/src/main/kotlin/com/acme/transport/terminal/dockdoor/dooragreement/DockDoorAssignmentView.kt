package com.acme.transport.terminal.dockdoor.dooragreement

/** How screens and exports show a dock door assignment. */
data class DockDoorAssignmentView(
    var codeText: String,
    var name: String,
    var label: String,
    var expiresAtText: String,
    var description: String,
    var updatedAt: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toDockDoorAssignmentView(item: DockDoorAssignment): DockDoorAssignmentView {
    return DockDoorAssignmentView(
        codeText = item.code,
        name = item.name,
        label = item.label,
        expiresAtText = item.expiresAt.toString(),
        description = item.description,
        updatedAt = item.updatedAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorAssignmentViews(items: MutableList<DockDoorAssignment>): MutableList<DockDoorAssignmentView> {
    val views = mutableListOf<DockDoorAssignmentView>()
    for (item in items) {
        views.add(toDockDoorAssignmentView(item))
    }
    return views
}

fun sumDockDoorAssignmentView(view: DockDoorAssignmentView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedUpdatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
