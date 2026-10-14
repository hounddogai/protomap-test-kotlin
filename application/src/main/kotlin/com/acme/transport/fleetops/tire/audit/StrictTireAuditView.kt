package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tire audit. */
data class StrictTireAuditView(
    var codeText: String,
    var labelText: String,
    var endsAt: String,
    var discount: String,
    var driverIdText: String,
    var reviewer: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toStrictTireAuditView(item: StrictTireAudit): StrictTireAuditView {
    return StrictTireAuditView(
        codeText = item.code,
        labelText = truncate(item.label, 64),
        endsAt = item.endsAt.toString(),
        discount = "%.2f".format(item.discount),
        driverIdText = truncate(item.driverId, 40),
        reviewer = item.reviewer,
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTireAuditViews(items: MutableList<StrictTireAudit>): MutableList<StrictTireAuditView> {
    val views = mutableListOf<StrictTireAuditView>()
    for (item in items) {
        views.add(toStrictTireAuditView(item))
    }
    return views
}

fun sumStrictTireAuditView(view: StrictTireAuditView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedEndsAt.toDouble() + parsedDiscount
}
