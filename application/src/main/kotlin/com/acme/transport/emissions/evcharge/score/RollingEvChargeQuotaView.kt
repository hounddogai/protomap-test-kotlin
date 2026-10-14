package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling ev charge quota. */
data class RollingEvChargeQuotaView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var discountText: String,
    var country: String,
    var ceiling: String,
    var summary: String,
    var notes: String,
    var floorValue: String,
    var threshold: String,
    var statusLabel: String,
)

fun toRollingEvChargeQuotaView(item: RollingEvChargeQuota): RollingEvChargeQuotaView {
    return RollingEvChargeQuotaView(
        codeText = item.code,
        title = truncate(item.title, 40),
        trailerId = item.trailerId,
        discountText = "%.2f".format(item.discount),
        country = truncate(item.country, 80),
        ceiling = "%.2f".format(item.ceiling),
        summary = item.summary,
        notes = item.notes,
        floorValue = "%.2f".format(item.floorValue),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingEvChargeQuotaViews(items: MutableList<RollingEvChargeQuota>): MutableList<RollingEvChargeQuotaView> {
    val views = mutableListOf<RollingEvChargeQuotaView>()
    for (item in items) {
        views.add(toRollingEvChargeQuotaView(item))
    }
    return views
}

fun sumRollingEvChargeQuotaView(view: RollingEvChargeQuotaView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedDiscount + parsedCeiling + parsedFloorValue
}
