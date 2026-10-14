package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire quota. */
data class TireQuotaView(
    var codeText: String,
    var label: String,
    var durationMinutesText: String,
    var endsAt: String,
    var phone: String,
    var priority: String,
    var approved: String,
    var floorValueText: String,
    var segment: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTireQuotaView(item: TireQuota): TireQuotaView {
    return TireQuotaView(
        codeText = item.code,
        label = item.label,
        durationMinutesText = item.durationMinutes.toString(),
        endsAt = item.endsAt.toString(),
        phone = item.phone,
        priority = item.priority.toString(),
        approved = "${item.approved}",
        floorValueText = "%.2f".format(item.floorValue),
        segment = truncate(item.segment, 64),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireQuotaViews(items: MutableList<TireQuota>): MutableList<TireQuotaView> {
    val views = mutableListOf<TireQuotaView>()
    for (item in items) {
        views.add(toTireQuotaView(item))
    }
    return views
}

fun sumTireQuotaView(view: TireQuotaView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble() + parsedPriority.toDouble()
}
