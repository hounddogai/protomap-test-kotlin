package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection schedule. */
data class InspectionScheduleView(
    var code: String,
    var titleText: String,
    var startsAtText: String,
    var source: String,
    var quantity: String,
    var threshold: String,
    var discount: String,
    var odometer: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toInspectionScheduleView(item: InspectionSchedule): InspectionScheduleView {
    return InspectionScheduleView(
        code = item.code,
        titleText = truncate(item.title, 120),
        startsAtText = item.startsAt.toString(),
        source = item.source,
        quantity = item.quantity.toString(),
        threshold = "%.2f".format(item.threshold),
        discount = "%.2f".format(item.discount),
        odometer = item.odometer.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionScheduleViews(items: MutableList<InspectionSchedule>): MutableList<InspectionScheduleView> {
    val views = mutableListOf<InspectionScheduleView>()
    for (item in items) {
        views.add(toInspectionScheduleView(item))
    }
    return views
}

fun sumInspectionScheduleView(view: InspectionScheduleView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedStartsAt.toDouble() + parsedQuantity.toDouble() + parsedThreshold
}
