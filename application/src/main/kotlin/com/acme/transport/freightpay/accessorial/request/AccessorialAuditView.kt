package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial audit. */
data class AccessorialAuditView(
    var code: String,
    var labelText: String,
    var locale: String,
    var pallets: String,
    var summary: String,
    var approved: String,
    var statusLabel: String,
)

fun toAccessorialAuditView(item: AccessorialAudit): AccessorialAuditView {
    return AccessorialAuditView(
        code = truncate(item.code, 64),
        labelText = item.label,
        locale = item.locale,
        pallets = item.pallets.toString(),
        summary = truncate(item.summary, 64),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialAuditViews(items: MutableList<AccessorialAudit>): MutableList<AccessorialAuditView> {
    val views = mutableListOf<AccessorialAuditView>()
    for (item in items) {
        views.add(toAccessorialAuditView(item))
    }
    return views
}

fun sumAccessorialAuditView(view: AccessorialAuditView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
