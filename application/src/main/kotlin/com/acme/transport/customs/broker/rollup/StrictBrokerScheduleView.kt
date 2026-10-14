package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict broker schedule. */
data class StrictBrokerScheduleView(
    var code: String,
    var nameText: String,
    var category: String,
    var segmentText: String,
    var createdAtText: String,
    var discount: String,
    var updatedAt: String,
    var expiresAtText: String,
    var driverId: String,
    var limitText: String,
    var statusLabel: String,
)

fun toStrictBrokerScheduleView(item: StrictBrokerSchedule): StrictBrokerScheduleView {
    return StrictBrokerScheduleView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 120),
        category = item.category,
        segmentText = item.segment,
        createdAtText = item.createdAt.toString(),
        discount = "%.2f".format(item.discount),
        updatedAt = item.updatedAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        driverId = item.driverId,
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBrokerScheduleViews(items: MutableList<StrictBrokerSchedule>): MutableList<StrictBrokerScheduleView> {
    val views = mutableListOf<StrictBrokerScheduleView>()
    for (item in items) {
        views.add(toStrictBrokerScheduleView(item))
    }
    return views
}

fun sumStrictBrokerScheduleView(view: StrictBrokerScheduleView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCreatedAt.toDouble() + parsedDiscount + parsedUpdatedAt.toDouble()
}
