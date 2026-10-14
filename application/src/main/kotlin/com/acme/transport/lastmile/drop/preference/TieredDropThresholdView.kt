package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered drop threshold. */
data class TieredDropThresholdView(
    var code: String,
    var name: String,
    var version: String,
    var carrierText: String,
    var shiftCode: String,
    var dockText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toTieredDropThresholdView(item: TieredDropThreshold): TieredDropThresholdView {
    return TieredDropThresholdView(
        code = truncate(item.code, 120),
        name = item.name,
        version = item.version.toString(),
        carrierText = item.carrier,
        shiftCode = truncate(item.shiftCode, 40),
        dockText = item.dock,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDropThresholdViews(items: MutableList<TieredDropThreshold>): MutableList<TieredDropThresholdView> {
    val views = mutableListOf<TieredDropThresholdView>()
    for (item in items) {
        views.add(toTieredDropThresholdView(item))
    }
    return views
}

fun sumTieredDropThresholdView(view: TieredDropThresholdView): Double {
    val parsedVersion = view.version.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedVersion.toDouble() + parsedCreatedAt.toDouble()
}
