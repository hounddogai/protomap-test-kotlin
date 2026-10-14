package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive audit manifest. */
data class AdaptiveAuditManifestView(
    var code: String,
    var name: String,
    var lane: String,
    var activeText: String,
    var channel: String,
    var unitText: String,
    var discount: String,
    var pinned: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toAdaptiveAuditManifestView(item: AdaptiveAuditManifest): AdaptiveAuditManifestView {
    return AdaptiveAuditManifestView(
        code = item.code,
        name = item.name,
        lane = truncate(item.lane, 64),
        activeText = "${item.active}",
        channel = item.channel,
        unitText = item.unit,
        discount = "%.2f".format(item.discount),
        pinned = "${item.pinned}",
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAuditManifestViews(items: MutableList<AdaptiveAuditManifest>): MutableList<AdaptiveAuditManifestView> {
    val views = mutableListOf<AdaptiveAuditManifestView>()
    for (item in items) {
        views.add(toAdaptiveAuditManifestView(item))
    }
    return views
}

fun sumAdaptiveAuditManifestView(view: AdaptiveAuditManifestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDiscount + parsedCeiling
}
