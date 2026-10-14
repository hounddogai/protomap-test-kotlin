package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered audit tier. */
data class TieredAuditTierView(
    var codeText: String,
    var name: String,
    var segment: String,
    var shiftCodeText: String,
    var version: String,
    var minimum: String,
    var country: String,
    var statusLabel: String,
)

fun toTieredAuditTierView(item: TieredAuditTier): TieredAuditTierView {
    return TieredAuditTierView(
        codeText = item.code,
        name = item.name,
        segment = truncate(item.segment, 120),
        shiftCodeText = item.shiftCode,
        version = item.version.toString(),
        minimum = "%.2f".format(item.minimum),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAuditTierViews(items: MutableList<TieredAuditTier>): MutableList<TieredAuditTierView> {
    val views = mutableListOf<TieredAuditTierView>()
    for (item in items) {
        views.add(toTieredAuditTierView(item))
    }
    return views
}

fun sumTieredAuditTierView(view: TieredAuditTierView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedVersion.toDouble() + parsedMinimum
}
