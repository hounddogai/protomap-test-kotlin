package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary lease return schedule. */
data class PrimaryLeaseReturnScheduleView(
    var code: String,
    var label: String,
    var source: String,
    var updatedAt: String,
    var timezone: String,
    var currency: String,
    var revision: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toPrimaryLeaseReturnScheduleView(item: PrimaryLeaseReturnSchedule): PrimaryLeaseReturnScheduleView {
    return PrimaryLeaseReturnScheduleView(
        code = item.code,
        label = item.label,
        source = item.source,
        updatedAt = item.updatedAt.toString(),
        timezone = item.timezone,
        currency = truncate(item.currency, 40),
        revision = item.revision.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryLeaseReturnScheduleViews(items: MutableList<PrimaryLeaseReturnSchedule>): MutableList<PrimaryLeaseReturnScheduleView> {
    val views = mutableListOf<PrimaryLeaseReturnScheduleView>()
    for (item in items) {
        views.add(toPrimaryLeaseReturnScheduleView(item))
    }
    return views
}

fun sumPrimaryLeaseReturnScheduleView(view: PrimaryLeaseReturnScheduleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedUpdatedAt.toDouble() + parsedRevision.toDouble()
}
