package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy audit manifest. */
data class LegacyAuditManifestView(
    var code: String,
    var labelText: String,
    var maximum: String,
    var approvedText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toLegacyAuditManifestView(item: LegacyAuditManifest): LegacyAuditManifestView {
    return LegacyAuditManifestView(
        code = item.code,
        labelText = item.label,
        maximum = "%.2f".format(item.maximum),
        approvedText = "${item.approved}",
        descriptionText = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAuditManifestViews(items: MutableList<LegacyAuditManifest>): MutableList<LegacyAuditManifestView> {
    val views = mutableListOf<LegacyAuditManifestView>()
    for (item in items) {
        views.add(toLegacyAuditManifestView(item))
    }
    return views
}

fun sumLegacyAuditManifestView(view: LegacyAuditManifestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
