package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling rest break milestone. */
data class RollingRestBreakMilestoneView(
    var code: String,
    var titleText: String,
    var sequenceText: String,
    var quantity: String,
    var durationMinutes: String,
    var timezone: String,
    var version: String,
    var thresholdText: String,
    var endsAt: String,
    var weight: String,
    var statusLabel: String,
)

fun toRollingRestBreakMilestoneView(item: RollingRestBreakMilestone): RollingRestBreakMilestoneView {
    return RollingRestBreakMilestoneView(
        code = item.code,
        titleText = truncate(item.title, 80),
        sequenceText = item.sequence.toString(),
        quantity = item.quantity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        timezone = truncate(item.timezone, 40),
        version = item.version.toString(),
        thresholdText = "%.2f".format(item.threshold),
        endsAt = item.endsAt.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRestBreakMilestoneViews(items: MutableList<RollingRestBreakMilestone>): MutableList<RollingRestBreakMilestoneView> {
    val views = mutableListOf<RollingRestBreakMilestoneView>()
    for (item in items) {
        views.add(toRollingRestBreakMilestoneView(item))
    }
    return views
}

fun sumRollingRestBreakMilestoneView(view: RollingRestBreakMilestoneView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedSequence.toDouble() + parsedQuantity.toDouble() + parsedDurationMinutes.toDouble()
}
