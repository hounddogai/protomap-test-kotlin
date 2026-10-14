package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability schedule. */
data class AvailabilityScheduleView(
    var codeText: String,
    var name: String,
    var shiftCodeText: String,
    var title: String,
    var expiresAt: String,
    var label: String,
    var currency: String,
    var approved: String,
    var summary: String,
    var statusLabel: String,
)

fun toAvailabilityScheduleView(item: AvailabilitySchedule): AvailabilityScheduleView {
    return AvailabilityScheduleView(
        codeText = item.code,
        name = item.name,
        shiftCodeText = item.shiftCode,
        title = truncate(item.title, 64),
        expiresAt = item.expiresAt.toString(),
        label = truncate(item.label, 40),
        currency = item.currency,
        approved = "${item.approved}",
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityScheduleViews(items: MutableList<AvailabilitySchedule>): MutableList<AvailabilityScheduleView> {
    val views = mutableListOf<AvailabilityScheduleView>()
    for (item in items) {
        views.add(toAvailabilityScheduleView(item))
    }
    return views
}

fun sumAvailabilityScheduleView(view: AvailabilityScheduleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedExpiresAt.toDouble()
}
