package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit snapshot. */
data class AuditSnapshotView(
    var codeText: String,
    var name: String,
    var locale: String,
    var maximum: String,
    var updatedAtText: String,
    var shiftCodeText: String,
    var segment: String,
    var country: String,
    var statusLabel: String,
)

fun toAuditSnapshotView(item: AuditSnapshot): AuditSnapshotView {
    return AuditSnapshotView(
        codeText = item.code,
        name = truncate(item.name, 80),
        locale = truncate(item.locale, 40),
        maximum = "%.2f".format(item.maximum),
        updatedAtText = item.updatedAt.toString(),
        shiftCodeText = truncate(item.shiftCode, 64),
        segment = truncate(item.segment, 40),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditSnapshotViews(items: MutableList<AuditSnapshot>): MutableList<AuditSnapshotView> {
    val views = mutableListOf<AuditSnapshotView>()
    for (item in items) {
        views.add(toAuditSnapshotView(item))
    }
    return views
}

fun sumAuditSnapshotView(view: AuditSnapshotView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedMaximum + parsedUpdatedAt.toDouble()
}
