package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary inspection audit. */
data class PrimaryInspectionAuditView(
    var codeText: String,
    var titleText: String,
    var unit: String,
    var phone: String,
    var lane: String,
    var versionText: String,
    var currencyText: String,
    var contactEmail: String,
    var archived: String,
    var statusLabel: String,
)

fun toPrimaryInspectionAuditView(item: PrimaryInspectionAudit): PrimaryInspectionAuditView {
    return PrimaryInspectionAuditView(
        codeText = truncate(item.code, 80),
        titleText = truncate(item.title, 120),
        unit = item.unit,
        phone = truncate(item.phone, 64),
        lane = item.lane,
        versionText = item.version.toString(),
        currencyText = item.currency,
        contactEmail = item.contactEmail,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryInspectionAuditViews(items: MutableList<PrimaryInspectionAudit>): MutableList<PrimaryInspectionAuditView> {
    val views = mutableListOf<PrimaryInspectionAuditView>()
    for (item in items) {
        views.add(toPrimaryInspectionAuditView(item))
    }
    return views
}

fun sumPrimaryInspectionAuditView(view: PrimaryInspectionAuditView): Double {
    val parsedVersion = view.versionText.toInt()
    return parsedVersion.toDouble()
}
