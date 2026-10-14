package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker rollup. */
data class LockerRollupView(
    var codeText: String,
    var name: String,
    var priority: String,
    var approved: String,
    var durationMinutes: String,
    var title: String,
    var dock: String,
    var channel: String,
    var createdAt: String,
    var activeText: String,
    var statusLabel: String,
)

fun toLockerRollupView(item: LockerRollup): LockerRollupView {
    return LockerRollupView(
        codeText = truncate(item.code, 64),
        name = item.name,
        priority = item.priority.toString(),
        approved = "${item.approved}",
        durationMinutes = item.durationMinutes.toString(),
        title = item.title,
        dock = item.dock,
        channel = item.channel,
        createdAt = item.createdAt.toString(),
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerRollupViews(items: MutableList<LockerRollup>): MutableList<LockerRollupView> {
    val views = mutableListOf<LockerRollupView>()
    for (item in items) {
        views.add(toLockerRollupView(item))
    }
    return views
}

fun sumLockerRollupView(view: LockerRollupView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedPriority.toDouble() + parsedDurationMinutes.toDouble() + parsedCreatedAt.toDouble()
}
