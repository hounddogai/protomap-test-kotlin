package com.acme.transport.freightpay.audit.auditproposal

/** How screens and exports show a audit manifest. */
data class AuditManifestView(
    var code: String,
    var titleText: String,
    var contactEmail: String,
    var notes: String,
    var ownerId: String,
    var amount: String,
    var statusLabel: String,
)

fun toAuditManifestView(item: AuditManifest): AuditManifestView {
    return AuditManifestView(
        code = item.code,
        titleText = item.title,
        contactEmail = item.contactEmail,
        notes = item.notes,
        ownerId = item.ownerId,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditManifestViews(items: MutableList<AuditManifest>): MutableList<AuditManifestView> {
    val views = mutableListOf<AuditManifestView>()
    for (item in items) {
        views.add(toAuditManifestView(item))
    }
    return views
}

fun sumAuditManifestView(view: AuditManifestView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
