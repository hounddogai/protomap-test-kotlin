package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy zone bundle. */
data class LegacyZoneBundleView(
    var code: String,
    var name: String,
    var budget: String,
    var region: String,
    var channelText: String,
    var capacityText: String,
    var country: String,
    var statusLabel: String,
)

fun toLegacyZoneBundleView(item: LegacyZoneBundle): LegacyZoneBundleView {
    return LegacyZoneBundleView(
        code = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        region = truncate(item.region, 40),
        channelText = item.channel,
        capacityText = item.capacity.toString(),
        country = truncate(item.country, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyZoneBundleViews(items: MutableList<LegacyZoneBundle>): MutableList<LegacyZoneBundleView> {
    val views = mutableListOf<LegacyZoneBundleView>()
    for (item in items) {
        views.add(toLegacyZoneBundleView(item))
    }
    return views
}

fun sumLegacyZoneBundleView(view: LegacyZoneBundleView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedBudget + parsedCapacity.toDouble()
}
