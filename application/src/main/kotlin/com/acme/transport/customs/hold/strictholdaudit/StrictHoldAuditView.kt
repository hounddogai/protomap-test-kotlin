package com.acme.transport.customs.hold.strictholdaudit

/** How screens and exports show a strict hold audit. */
data class StrictHoldAuditView(
    var codeText: String,
    var label: String,
    var region: String,
    var revision: String,
    var notes: String,
    var statusLabel: String,
)

fun toStrictHoldAuditView(item: StrictHoldAudit): StrictHoldAuditView {
    return StrictHoldAuditView(
        codeText = item.code,
        label = item.label,
        region = item.region,
        revision = item.revision.toString(),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictHoldAuditViews(items: MutableList<StrictHoldAudit>): MutableList<StrictHoldAuditView> {
    val views = mutableListOf<StrictHoldAuditView>()
    for (item in items) {
        views.add(toStrictHoldAuditView(item))
    }
    return views
}

fun sumStrictHoldAuditView(view: StrictHoldAuditView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
