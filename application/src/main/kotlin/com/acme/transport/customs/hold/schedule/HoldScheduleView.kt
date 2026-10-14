package com.acme.transport.customs.hold.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold schedule. */
data class HoldScheduleView(
    var codeText: String,
    var title: String,
    var contactEmail: String,
    var priorityText: String,
    var trailerId: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toHoldScheduleView(item: HoldSchedule): HoldScheduleView {
    return HoldScheduleView(
        codeText = truncate(item.code, 64),
        title = item.title,
        contactEmail = item.contactEmail,
        priorityText = item.priority.toString(),
        trailerId = truncate(item.trailerId, 40),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldScheduleViews(items: MutableList<HoldSchedule>): MutableList<HoldScheduleView> {
    val views = mutableListOf<HoldScheduleView>()
    for (item in items) {
        views.add(toHoldScheduleView(item))
    }
    return views
}

fun sumHoldScheduleView(view: HoldScheduleView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedPriority.toDouble() + parsedBatchSize.toDouble()
}
