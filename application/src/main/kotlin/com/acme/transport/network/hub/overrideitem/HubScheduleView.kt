package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub schedule. */
data class HubScheduleView(
    var code: String,
    var title: String,
    var timezone: String,
    var createdAt: String,
    var discount: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toHubScheduleView(item: HubSchedule): HubScheduleView {
    return HubScheduleView(
        code = item.code,
        title = truncate(item.title, 80),
        timezone = truncate(item.timezone, 80),
        createdAt = item.createdAt.toString(),
        discount = "%.2f".format(item.discount),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubScheduleViews(items: MutableList<HubSchedule>): MutableList<HubScheduleView> {
    val views = mutableListOf<HubScheduleView>()
    for (item in items) {
        views.add(toHubScheduleView(item))
    }
    return views
}

fun sumHubScheduleView(view: HubScheduleView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedCreatedAt.toDouble() + parsedDiscount + parsedMinimum
}
