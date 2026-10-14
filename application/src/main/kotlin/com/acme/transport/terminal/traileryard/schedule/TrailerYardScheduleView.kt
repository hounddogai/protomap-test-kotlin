package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard schedule. */
data class TrailerYardScheduleView(
    var codeText: String,
    var title: String,
    var expiresAt: String,
    var budget: String,
    var categoryText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toTrailerYardScheduleView(item: TrailerYardSchedule): TrailerYardScheduleView {
    return TrailerYardScheduleView(
        codeText = truncate(item.code, 40),
        title = truncate(item.title, 80),
        expiresAt = item.expiresAt.toString(),
        budget = "%.2f".format(item.budget),
        categoryText = truncate(item.category, 120),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardScheduleViews(items: MutableList<TrailerYardSchedule>): MutableList<TrailerYardScheduleView> {
    val views = mutableListOf<TrailerYardScheduleView>()
    for (item in items) {
        views.add(toTrailerYardScheduleView(item))
    }
    return views
}

fun sumTrailerYardScheduleView(view: TrailerYardScheduleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedExpiresAt.toDouble() + parsedBudget + parsedStopCount.toDouble()
}
