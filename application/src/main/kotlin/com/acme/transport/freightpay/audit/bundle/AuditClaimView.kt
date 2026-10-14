package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit claim. */
data class AuditClaimView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var floorValue: String,
    var visibleText: String,
    var activeText: String,
    var versionText: String,
    var statusLabel: String,
)

fun toAuditClaimView(item: AuditClaim): AuditClaimView {
    return AuditClaimView(
        code = truncate(item.code, 120),
        label = item.label,
        contactEmail = item.contactEmail,
        floorValue = "%.2f".format(item.floorValue),
        visibleText = "${item.visible}",
        activeText = "${item.active}",
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditClaimViews(items: MutableList<AuditClaim>): MutableList<AuditClaimView> {
    val views = mutableListOf<AuditClaimView>()
    for (item in items) {
        views.add(toAuditClaimView(item))
    }
    return views
}

fun sumAuditClaimView(view: AuditClaimView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedFloorValue + parsedVersion.toDouble()
}
