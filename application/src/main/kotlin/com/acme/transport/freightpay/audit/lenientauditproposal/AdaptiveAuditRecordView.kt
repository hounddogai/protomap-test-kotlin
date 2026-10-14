package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive audit record. */
data class AdaptiveAuditRecordView(
    var codeText: String,
    var labelText: String,
    var pinned: String,
    var etaMinutesText: String,
    var versionText: String,
    var statusLabel: String,
)

fun toAdaptiveAuditRecordView(item: AdaptiveAuditRecord): AdaptiveAuditRecordView {
    return AdaptiveAuditRecordView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 120),
        pinned = "${item.pinned}",
        etaMinutesText = item.etaMinutes.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAuditRecordViews(items: MutableList<AdaptiveAuditRecord>): MutableList<AdaptiveAuditRecordView> {
    val views = mutableListOf<AdaptiveAuditRecordView>()
    for (item in items) {
        views.add(toAdaptiveAuditRecordView(item))
    }
    return views
}

fun sumAdaptiveAuditRecordView(view: AdaptiveAuditRecordView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedEtaMinutes.toDouble() + parsedVersion.toDouble()
}
