package com.acme.transport.crewing.dutyperiod.ledger

/** How screens and exports show a lenient duty period profile. */
data class LenientDutyPeriodProfileView(
    var code: String,
    var nameText: String,
    var pinned: String,
    var timezone: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toLenientDutyPeriodProfileView(item: LenientDutyPeriodProfile): LenientDutyPeriodProfileView {
    return LenientDutyPeriodProfileView(
        code = item.code,
        nameText = item.name,
        pinned = "${item.pinned}",
        timezone = item.timezone,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDutyPeriodProfileViews(items: MutableList<LenientDutyPeriodProfile>): MutableList<LenientDutyPeriodProfileView> {
    val views = mutableListOf<LenientDutyPeriodProfileView>()
    for (item in items) {
        views.add(toLenientDutyPeriodProfileView(item))
    }
    return views
}

fun sumLenientDutyPeriodProfileView(view: LenientDutyPeriodProfileView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
