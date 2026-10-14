package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient capacity milestone. */
data class LenientCapacityMilestoneView(
    var code: String,
    var label: String,
    var active: String,
    var titleText: String,
    var createdAtText: String,
    var notes: String,
    var source: String,
    var durationMinutes: String,
    var floorValue: String,
    var sequenceText: String,
    var limit: String,
    var statusLabel: String,
)

fun toLenientCapacityMilestoneView(item: LenientCapacityMilestone): LenientCapacityMilestoneView {
    return LenientCapacityMilestoneView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 64),
        active = "${item.active}",
        titleText = truncate(item.title, 64),
        createdAtText = item.createdAt.toString(),
        notes = truncate(item.notes, 80),
        source = truncate(item.source, 64),
        durationMinutes = item.durationMinutes.toString(),
        floorValue = "%.2f".format(item.floorValue),
        sequenceText = item.sequence.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCapacityMilestoneViews(items: MutableList<LenientCapacityMilestone>): MutableList<LenientCapacityMilestoneView> {
    val views = mutableListOf<LenientCapacityMilestoneView>()
    for (item in items) {
        views.add(toLenientCapacityMilestoneView(item))
    }
    return views
}

fun sumLenientCapacityMilestoneView(view: LenientCapacityMilestoneView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCreatedAt.toDouble() + parsedDurationMinutes.toDouble() + parsedFloorValue
}
