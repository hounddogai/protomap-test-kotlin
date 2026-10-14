package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped sortation milestone. */
data class CappedSortationMilestoneView(
    var code: String,
    var labelText: String,
    var ownerId: String,
    var pinned: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toCappedSortationMilestoneView(item: CappedSortationMilestone): CappedSortationMilestoneView {
    return CappedSortationMilestoneView(
        code = truncate(item.code, 40),
        labelText = item.label,
        ownerId = truncate(item.ownerId, 40),
        pinned = "${item.pinned}",
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedSortationMilestoneViews(items: MutableList<CappedSortationMilestone>): MutableList<CappedSortationMilestoneView> {
    val views = mutableListOf<CappedSortationMilestoneView>()
    for (item in items) {
        views.add(toCappedSortationMilestoneView(item))
    }
    return views
}

fun sumCappedSortationMilestoneView(view: CappedSortationMilestoneView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
