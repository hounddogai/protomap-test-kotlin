package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled backhaul audit. */
data class ScheduledBackhaulAuditView(
    var codeText: String,
    var labelText: String,
    var durationMinutes: String,
    var regionText: String,
    var rate: String,
    var phoneText: String,
    var segment: String,
    var updatedAt: String,
    var capacity: String,
    var statusLabel: String,
)

fun toScheduledBackhaulAuditView(item: ScheduledBackhaulAudit): ScheduledBackhaulAuditView {
    return ScheduledBackhaulAuditView(
        codeText = truncate(item.code, 64),
        labelText = truncate(item.label, 80),
        durationMinutes = item.durationMinutes.toString(),
        regionText = truncate(item.region, 40),
        rate = "%.2f".format(item.rate),
        phoneText = truncate(item.phone, 120),
        segment = item.segment,
        updatedAt = item.updatedAt.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledBackhaulAuditViews(items: MutableList<ScheduledBackhaulAudit>): MutableList<ScheduledBackhaulAuditView> {
    val views = mutableListOf<ScheduledBackhaulAuditView>()
    for (item in items) {
        views.add(toScheduledBackhaulAuditView(item))
    }
    return views
}

fun sumScheduledBackhaulAuditView(view: ScheduledBackhaulAuditView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedRate + parsedUpdatedAt.toDouble()
}
