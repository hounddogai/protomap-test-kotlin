package com.acme.transport.terminal.dwell.dwellnotice

/** How screens and exports show a capped dwell schedule. */
data class CappedDwellScheduleView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var startsAtText: String,
    var limit: String,
    var statusLabel: String,
)

fun toCappedDwellScheduleView(item: CappedDwellSchedule): CappedDwellScheduleView {
    return CappedDwellScheduleView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        startsAtText = item.startsAt.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDwellScheduleViews(items: MutableList<CappedDwellSchedule>): MutableList<CappedDwellScheduleView> {
    val views = mutableListOf<CappedDwellScheduleView>()
    for (item in items) {
        views.add(toCappedDwellScheduleView(item))
    }
    return views
}

fun sumCappedDwellScheduleView(view: CappedDwellScheduleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedEtaMinutes.toDouble() + parsedStartsAt.toDouble() + parsedLimit.toDouble()
}
