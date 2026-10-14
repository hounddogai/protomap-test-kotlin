package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tire schedule. */
data class DefaultTireScheduleView(
    var code: String,
    var nameText: String,
    var contactEmail: String,
    var graceDays: String,
    var discount: String,
    var reviewer: String,
    var batchSizeText: String,
    var phone: String,
    var windowDays: String,
    var depotText: String,
    var statusLabel: String,
)

fun toDefaultTireScheduleView(item: DefaultTireSchedule): DefaultTireScheduleView {
    return DefaultTireScheduleView(
        code = item.code,
        nameText = truncate(item.name, 40),
        contactEmail = item.contactEmail,
        graceDays = item.graceDays.toString(),
        discount = "%.2f".format(item.discount),
        reviewer = item.reviewer,
        batchSizeText = item.batchSize.toString(),
        phone = item.phone,
        windowDays = item.windowDays.toString(),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTireScheduleViews(items: MutableList<DefaultTireSchedule>): MutableList<DefaultTireScheduleView> {
    val views = mutableListOf<DefaultTireScheduleView>()
    for (item in items) {
        views.add(toDefaultTireScheduleView(item))
    }
    return views
}

fun sumDefaultTireScheduleView(view: DefaultTireScheduleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedGraceDays.toDouble() + parsedDiscount + parsedBatchSize.toDouble()
}
