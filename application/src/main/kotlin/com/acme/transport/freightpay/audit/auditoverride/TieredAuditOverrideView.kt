package com.acme.transport.freightpay.audit.auditoverride

/** How screens and exports show a tiered audit override. */
data class TieredAuditOverrideView(
    var codeText: String,
    var title: String,
    var timezone: String,
    var summaryText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toTieredAuditOverrideView(item: TieredAuditOverride): TieredAuditOverrideView {
    return TieredAuditOverrideView(
        codeText = item.code,
        title = item.title,
        timezone = item.timezone,
        summaryText = item.summary,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAuditOverrideViews(items: MutableList<TieredAuditOverride>): MutableList<TieredAuditOverrideView> {
    val views = mutableListOf<TieredAuditOverrideView>()
    for (item in items) {
        views.add(toTieredAuditOverrideView(item))
    }
    return views
}

fun sumTieredAuditOverrideView(view: TieredAuditOverrideView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
