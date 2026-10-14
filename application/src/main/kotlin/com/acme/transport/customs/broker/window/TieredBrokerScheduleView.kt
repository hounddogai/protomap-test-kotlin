package com.acme.transport.customs.broker.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered broker schedule. */
data class TieredBrokerScheduleView(
    var code: String,
    var label: String,
    var windowDays: String,
    var reviewer: String,
    var archivedText: String,
    var minimum: String,
    var approved: String,
    var weightText: String,
    var ownerIdText: String,
    var priorityText: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toTieredBrokerScheduleView(item: TieredBrokerSchedule): TieredBrokerScheduleView {
    return TieredBrokerScheduleView(
        code = truncate(item.code, 40),
        label = item.label,
        windowDays = item.windowDays.toString(),
        reviewer = item.reviewer,
        archivedText = "${item.archived}",
        minimum = "%.2f".format(item.minimum),
        approved = "${item.approved}",
        weightText = "%.2f".format(item.weight),
        ownerIdText = truncate(item.ownerId, 40),
        priorityText = item.priority.toString(),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredBrokerScheduleViews(items: MutableList<TieredBrokerSchedule>): MutableList<TieredBrokerScheduleView> {
    val views = mutableListOf<TieredBrokerScheduleView>()
    for (item in items) {
        views.add(toTieredBrokerScheduleView(item))
    }
    return views
}

fun sumTieredBrokerScheduleView(view: TieredBrokerScheduleView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedWindowDays.toDouble() + parsedMinimum + parsedWeight
}
