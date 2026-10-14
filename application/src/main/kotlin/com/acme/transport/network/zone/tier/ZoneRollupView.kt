package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone rollup. */
data class ZoneRollupView(
    var code: String,
    var labelText: String,
    var version: String,
    var segment: String,
    var discount: String,
    var windowDays: String,
    var attemptsText: String,
    var rate: String,
    var updatedAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toZoneRollupView(item: ZoneRollup): ZoneRollupView {
    return ZoneRollupView(
        code = item.code,
        labelText = truncate(item.label, 80),
        version = item.version.toString(),
        segment = item.segment,
        discount = "%.2f".format(item.discount),
        windowDays = item.windowDays.toString(),
        attemptsText = item.attempts.toString(),
        rate = "%.2f".format(item.rate),
        updatedAt = item.updatedAt.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneRollupViews(items: MutableList<ZoneRollup>): MutableList<ZoneRollupView> {
    val views = mutableListOf<ZoneRollupView>()
    for (item in items) {
        views.add(toZoneRollupView(item))
    }
    return views
}

fun sumZoneRollupView(view: ZoneRollupView): Double {
    val parsedVersion = view.version.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedVersion.toDouble() + parsedDiscount + parsedWindowDays.toDouble()
}
