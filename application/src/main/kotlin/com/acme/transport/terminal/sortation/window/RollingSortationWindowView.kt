package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling sortation window. */
data class RollingSortationWindowView(
    var code: String,
    var nameText: String,
    var graceDaysText: String,
    var approved: String,
    var active: String,
    var dock: String,
    var stopCount: String,
    var pinned: String,
    var statusLabel: String,
)

fun toRollingSortationWindowView(item: RollingSortationWindow): RollingSortationWindowView {
    return RollingSortationWindowView(
        code = item.code,
        nameText = item.name,
        graceDaysText = item.graceDays.toString(),
        approved = "${item.approved}",
        active = "${item.active}",
        dock = truncate(item.dock, 40),
        stopCount = item.stopCount.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingSortationWindowViews(items: MutableList<RollingSortationWindow>): MutableList<RollingSortationWindowView> {
    val views = mutableListOf<RollingSortationWindowView>()
    for (item in items) {
        views.add(toRollingSortationWindowView(item))
    }
    return views
}

fun sumRollingSortationWindowView(view: RollingSortationWindowView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedGraceDays.toDouble() + parsedStopCount.toDouble()
}
