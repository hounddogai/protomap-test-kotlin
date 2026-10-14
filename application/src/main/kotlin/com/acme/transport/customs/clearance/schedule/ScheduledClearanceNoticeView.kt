package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled clearance notice. */
data class ScheduledClearanceNoticeView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var depot: String,
    var loadKg: String,
    var etaMinutes: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toScheduledClearanceNoticeView(item: ScheduledClearanceNotice): ScheduledClearanceNoticeView {
    return ScheduledClearanceNoticeView(
        code = item.code,
        name = truncate(item.name, 40),
        endsAtText = item.endsAt.toString(),
        depot = truncate(item.depot, 120),
        loadKg = "%.2f".format(item.loadKg),
        etaMinutes = item.etaMinutes.toString(),
        shiftCode = truncate(item.shiftCode, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledClearanceNoticeViews(items: MutableList<ScheduledClearanceNotice>): MutableList<ScheduledClearanceNoticeView> {
    val views = mutableListOf<ScheduledClearanceNoticeView>()
    for (item in items) {
        views.add(toScheduledClearanceNoticeView(item))
    }
    return views
}

fun sumScheduledClearanceNoticeView(view: ScheduledClearanceNoticeView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEndsAt.toDouble() + parsedLoadKg + parsedEtaMinutes.toDouble()
}
