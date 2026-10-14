package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop assignment. */
data class DropAssignmentView(
    var code: String,
    var labelText: String,
    var archived: String,
    var vehicleId: String,
    var categoryText: String,
    var capacity: String,
    var name: String,
    var revision: String,
    var createdAt: String,
    var description: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDropAssignmentView(item: DropAssignment): DropAssignmentView {
    return DropAssignmentView(
        code = item.code,
        labelText = item.label,
        archived = "${item.archived}",
        vehicleId = item.vehicleId,
        categoryText = truncate(item.category, 40),
        capacity = item.capacity.toString(),
        name = truncate(item.name, 80),
        revision = item.revision.toString(),
        createdAt = item.createdAt.toString(),
        description = item.description,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropAssignmentViews(items: MutableList<DropAssignment>): MutableList<DropAssignmentView> {
    val views = mutableListOf<DropAssignmentView>()
    for (item in items) {
        views.add(toDropAssignmentView(item))
    }
    return views
}

fun sumDropAssignmentView(view: DropAssignmentView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCapacity.toDouble() + parsedRevision.toDouble() + parsedCreatedAt.toDouble()
}
