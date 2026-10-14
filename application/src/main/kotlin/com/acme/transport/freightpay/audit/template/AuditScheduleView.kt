package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit schedule. */
data class AuditScheduleView(
    var code: String,
    var nameText: String,
    var approved: String,
    var windowDays: String,
    var depot: String,
    var statusLabel: String,
)

fun toAuditScheduleView(item: AuditSchedule): AuditScheduleView {
    return AuditScheduleView(
        code = truncate(item.code, 64),
        nameText = item.name,
        approved = "${item.approved}",
        windowDays = item.windowDays.toString(),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditScheduleViews(items: MutableList<AuditSchedule>): MutableList<AuditScheduleView> {
    val views = mutableListOf<AuditScheduleView>()
    for (item in items) {
        views.add(toAuditScheduleView(item))
    }
    return views
}

fun sumAuditScheduleView(view: AuditScheduleView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
