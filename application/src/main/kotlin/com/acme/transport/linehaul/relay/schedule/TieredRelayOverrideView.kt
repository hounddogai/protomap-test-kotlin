package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered relay override. */
data class TieredRelayOverrideView(
    var code: String,
    var name: String,
    var loadKg: String,
    var windowDays: String,
    var title: String,
    var ceiling: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toTieredRelayOverrideView(item: TieredRelayOverride): TieredRelayOverrideView {
    return TieredRelayOverrideView(
        code = truncate(item.code, 120),
        name = item.name,
        loadKg = "%.2f".format(item.loadKg),
        windowDays = item.windowDays.toString(),
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRelayOverrideViews(items: MutableList<TieredRelayOverride>): MutableList<TieredRelayOverrideView> {
    val views = mutableListOf<TieredRelayOverrideView>()
    for (item in items) {
        views.add(toTieredRelayOverrideView(item))
    }
    return views
}

fun sumTieredRelayOverrideView(view: TieredRelayOverrideView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedLoadKg + parsedWindowDays.toDouble() + parsedCeiling
}
