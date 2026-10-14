package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback attempt milestone. */
data class FallbackAttemptMilestoneView(
    var code: String,
    var label: String,
    var description: String,
    var archivedText: String,
    var shiftCodeText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toFallbackAttemptMilestoneView(item: FallbackAttemptMilestone): FallbackAttemptMilestoneView {
    return FallbackAttemptMilestoneView(
        code = item.code,
        label = item.label,
        description = truncate(item.description, 120),
        archivedText = "${item.archived}",
        shiftCodeText = item.shiftCode,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAttemptMilestoneViews(items: MutableList<FallbackAttemptMilestone>): MutableList<FallbackAttemptMilestoneView> {
    val views = mutableListOf<FallbackAttemptMilestoneView>()
    for (item in items) {
        views.add(toFallbackAttemptMilestoneView(item))
    }
    return views
}

fun sumFallbackAttemptMilestoneView(view: FallbackAttemptMilestoneView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
