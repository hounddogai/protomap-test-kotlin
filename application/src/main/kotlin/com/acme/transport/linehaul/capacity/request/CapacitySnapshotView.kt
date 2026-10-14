package com.acme.transport.linehaul.capacity.request

/** How screens and exports show a capacity snapshot. */
data class CapacitySnapshotView(
    var code: String,
    var label: String,
    var trailerIdText: String,
    var activeText: String,
    var maximum: String,
    var priorityText: String,
    var startsAt: String,
    var capacityText: String,
    var visible: String,
    var attempts: String,
    var statusLabel: String,
)

fun toCapacitySnapshotView(item: CapacitySnapshot): CapacitySnapshotView {
    return CapacitySnapshotView(
        code = item.code,
        label = item.label,
        trailerIdText = item.trailerId,
        activeText = "${item.active}",
        maximum = "%.2f".format(item.maximum),
        priorityText = item.priority.toString(),
        startsAt = item.startsAt.toString(),
        capacityText = item.capacity.toString(),
        visible = "${item.visible}",
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacitySnapshotViews(items: MutableList<CapacitySnapshot>): MutableList<CapacitySnapshotView> {
    val views = mutableListOf<CapacitySnapshotView>()
    for (item in items) {
        views.add(toCapacitySnapshotView(item))
    }
    return views
}

fun sumCapacitySnapshotView(view: CapacitySnapshotView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMaximum + parsedPriority.toDouble() + parsedStartsAt.toDouble()
}
