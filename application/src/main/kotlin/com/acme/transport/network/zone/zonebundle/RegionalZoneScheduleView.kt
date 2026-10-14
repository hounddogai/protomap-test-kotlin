package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional zone schedule. */
data class RegionalZoneScheduleView(
    var codeText: String,
    var titleText: String,
    var locale: String,
    var loadKg: String,
    var amount: String,
    var windowDays: String,
    var country: String,
    var carrier: String,
    var maximum: String,
    var statusLabel: String,
)

fun toRegionalZoneScheduleView(item: RegionalZoneSchedule): RegionalZoneScheduleView {
    return RegionalZoneScheduleView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        locale = item.locale,
        loadKg = "%.2f".format(item.loadKg),
        amount = "%.2f".format(item.amount),
        windowDays = item.windowDays.toString(),
        country = item.country,
        carrier = truncate(item.carrier, 120),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalZoneScheduleViews(items: MutableList<RegionalZoneSchedule>): MutableList<RegionalZoneScheduleView> {
    val views = mutableListOf<RegionalZoneScheduleView>()
    for (item in items) {
        views.add(toRegionalZoneScheduleView(item))
    }
    return views
}

fun sumRegionalZoneScheduleView(view: RegionalZoneScheduleView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedLoadKg + parsedAmount + parsedWindowDays.toDouble()
}
