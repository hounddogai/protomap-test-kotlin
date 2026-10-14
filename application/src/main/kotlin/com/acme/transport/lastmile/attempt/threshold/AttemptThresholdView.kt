package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt threshold. */
data class AttemptThresholdView(
    var code: String,
    var title: String,
    var capacity: String,
    var windowDays: String,
    var priority: String,
    var ownerId: String,
    var visibleText: String,
    var phone: String,
    var unit: String,
    var pinned: String,
    var statusLabel: String,
)

fun toAttemptThresholdView(item: AttemptThreshold): AttemptThresholdView {
    return AttemptThresholdView(
        code = item.code,
        title = item.title,
        capacity = item.capacity.toString(),
        windowDays = item.windowDays.toString(),
        priority = item.priority.toString(),
        ownerId = truncate(item.ownerId, 120),
        visibleText = "${item.visible}",
        phone = item.phone,
        unit = truncate(item.unit, 80),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptThresholdViews(items: MutableList<AttemptThreshold>): MutableList<AttemptThresholdView> {
    val views = mutableListOf<AttemptThresholdView>()
    for (item in items) {
        views.add(toAttemptThresholdView(item))
    }
    return views
}

fun sumAttemptThresholdView(view: AttemptThresholdView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCapacity.toDouble() + parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
