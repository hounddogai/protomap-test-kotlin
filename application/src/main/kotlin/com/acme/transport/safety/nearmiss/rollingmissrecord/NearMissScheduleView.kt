package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss schedule. */
data class NearMissScheduleView(
    var code: String,
    var name: String,
    var approved: String,
    var etaMinutesText: String,
    var timezone: String,
    var windowDays: String,
    var amountText: String,
    var weight: String,
    var carrier: String,
    var region: String,
    var statusLabel: String,
)

fun toNearMissScheduleView(item: NearMissSchedule): NearMissScheduleView {
    return NearMissScheduleView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 80),
        approved = "${item.approved}",
        etaMinutesText = item.etaMinutes.toString(),
        timezone = item.timezone,
        windowDays = item.windowDays.toString(),
        amountText = "%.2f".format(item.amount),
        weight = "%.2f".format(item.weight),
        carrier = item.carrier,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissScheduleViews(items: MutableList<NearMissSchedule>): MutableList<NearMissScheduleView> {
    val views = mutableListOf<NearMissScheduleView>()
    for (item in items) {
        views.add(toNearMissScheduleView(item))
    }
    return views
}

fun sumNearMissScheduleView(view: NearMissScheduleView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble() + parsedAmount
}
