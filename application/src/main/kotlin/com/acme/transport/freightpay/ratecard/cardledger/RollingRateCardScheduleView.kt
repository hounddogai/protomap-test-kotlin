package com.acme.transport.freightpay.ratecard.cardledger

/** How screens and exports show a rolling rate card schedule. */
data class RollingRateCardScheduleView(
    var code: String,
    var label: String,
    var locale: String,
    var windowDays: String,
    var lane: String,
    var shiftCode: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toRollingRateCardScheduleView(item: RollingRateCardSchedule): RollingRateCardScheduleView {
    return RollingRateCardScheduleView(
        code = item.code,
        label = item.label,
        locale = item.locale,
        windowDays = item.windowDays.toString(),
        lane = item.lane,
        shiftCode = item.shiftCode,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRateCardScheduleViews(items: MutableList<RollingRateCardSchedule>): MutableList<RollingRateCardScheduleView> {
    val views = mutableListOf<RollingRateCardScheduleView>()
    for (item in items) {
        views.add(toRollingRateCardScheduleView(item))
    }
    return views
}

fun sumRollingRateCardScheduleView(view: RollingRateCardScheduleView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
