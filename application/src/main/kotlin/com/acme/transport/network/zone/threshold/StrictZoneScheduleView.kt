package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict zone schedule. */
data class StrictZoneScheduleView(
    var codeText: String,
    var labelText: String,
    var notes: String,
    var createdAt: String,
    var region: String,
    var discount: String,
    var reviewer: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toStrictZoneScheduleView(item: StrictZoneSchedule): StrictZoneScheduleView {
    return StrictZoneScheduleView(
        codeText = item.code,
        labelText = item.label,
        notes = truncate(item.notes, 80),
        createdAt = item.createdAt.toString(),
        region = item.region,
        discount = "%.2f".format(item.discount),
        reviewer = item.reviewer,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictZoneScheduleViews(items: MutableList<StrictZoneSchedule>): MutableList<StrictZoneScheduleView> {
    val views = mutableListOf<StrictZoneScheduleView>()
    for (item in items) {
        views.add(toStrictZoneScheduleView(item))
    }
    return views
}

fun sumStrictZoneScheduleView(view: StrictZoneScheduleView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCreatedAt.toDouble() + parsedDiscount + parsedLoadKg
}
