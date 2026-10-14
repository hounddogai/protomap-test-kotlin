package com.acme.transport.terminal.dwell.dwelldigest

/** How screens and exports show a scheduled dwell adjustment. */
data class ScheduledDwellAdjustmentView(
    var codeText: String,
    var title: String,
    var sequenceText: String,
    var updatedAtText: String,
    var minimumText: String,
    var laneText: String,
    var statusLabel: String,
)

fun toScheduledDwellAdjustmentView(item: ScheduledDwellAdjustment): ScheduledDwellAdjustmentView {
    return ScheduledDwellAdjustmentView(
        codeText = item.code,
        title = item.title,
        sequenceText = item.sequence.toString(),
        updatedAtText = item.updatedAt.toString(),
        minimumText = "%.2f".format(item.minimum),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDwellAdjustmentViews(items: MutableList<ScheduledDwellAdjustment>): MutableList<ScheduledDwellAdjustmentView> {
    val views = mutableListOf<ScheduledDwellAdjustmentView>()
    for (item in items) {
        views.add(toScheduledDwellAdjustmentView(item))
    }
    return views
}

fun sumScheduledDwellAdjustmentView(view: ScheduledDwellAdjustmentView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedSequence.toDouble() + parsedUpdatedAt.toDouble() + parsedMinimum
}
