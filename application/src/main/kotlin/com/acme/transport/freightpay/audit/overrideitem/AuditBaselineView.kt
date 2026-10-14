package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit baseline. */
data class AuditBaselineView(
    var code: String,
    var labelText: String,
    var visible: String,
    var titleText: String,
    var contactEmail: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toAuditBaselineView(item: AuditBaseline): AuditBaselineView {
    return AuditBaselineView(
        code = item.code,
        labelText = truncate(item.label, 64),
        visible = "${item.visible}",
        titleText = item.title,
        contactEmail = item.contactEmail,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditBaselineViews(items: MutableList<AuditBaseline>): MutableList<AuditBaselineView> {
    val views = mutableListOf<AuditBaselineView>()
    for (item in items) {
        views.add(toAuditBaselineView(item))
    }
    return views
}

fun sumAuditBaselineView(view: AuditBaselineView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedFuelLiters
}
