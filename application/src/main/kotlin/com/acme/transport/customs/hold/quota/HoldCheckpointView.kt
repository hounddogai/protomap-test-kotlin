package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold checkpoint. */
data class HoldCheckpointView(
    var code: String,
    var name: String,
    var visible: String,
    var endsAt: String,
    var priorityText: String,
    var depot: String,
    var statusLabel: String,
)

fun toHoldCheckpointView(item: HoldCheckpoint): HoldCheckpointView {
    return HoldCheckpointView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 64),
        visible = "${item.visible}",
        endsAt = item.endsAt.toString(),
        priorityText = item.priority.toString(),
        depot = truncate(item.depot, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldCheckpointViews(items: MutableList<HoldCheckpoint>): MutableList<HoldCheckpointView> {
    val views = mutableListOf<HoldCheckpointView>()
    for (item in items) {
        views.add(toHoldCheckpointView(item))
    }
    return views
}

fun sumHoldCheckpointView(view: HoldCheckpointView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedEndsAt.toDouble() + parsedPriority.toDouble()
}
