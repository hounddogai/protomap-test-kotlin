package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched capacity schedule. */
data class BatchedCapacityScheduleView(
    var code: String,
    var title: String,
    var minimum: String,
    var rateText: String,
    var lane: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toBatchedCapacityScheduleView(item: BatchedCapacitySchedule): BatchedCapacityScheduleView {
    return BatchedCapacityScheduleView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 80),
        minimum = "%.2f".format(item.minimum),
        rateText = "%.2f".format(item.rate),
        lane = truncate(item.lane, 64),
        timezoneText = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityScheduleViews(items: MutableList<BatchedCapacitySchedule>): MutableList<BatchedCapacityScheduleView> {
    val views = mutableListOf<BatchedCapacityScheduleView>()
    for (item in items) {
        views.add(toBatchedCapacityScheduleView(item))
    }
    return views
}

fun sumBatchedCapacityScheduleView(view: BatchedCapacityScheduleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedMinimum + parsedRate
}
