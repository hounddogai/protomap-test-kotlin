package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster audit. */
data class RosterAuditView(
    var codeText: String,
    var labelText: String,
    var phoneText: String,
    var approved: String,
    var segment: String,
    var odometer: String,
    var statusLabel: String,
)

fun toRosterAuditView(item: RosterAudit): RosterAuditView {
    return RosterAuditView(
        codeText = truncate(item.code, 80),
        labelText = truncate(item.label, 120),
        phoneText = truncate(item.phone, 120),
        approved = "${item.approved}",
        segment = item.segment,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterAuditViews(items: MutableList<RosterAudit>): MutableList<RosterAuditView> {
    val views = mutableListOf<RosterAuditView>()
    for (item in items) {
        views.add(toRosterAuditView(item))
    }
    return views
}

fun sumRosterAuditView(view: RosterAuditView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
