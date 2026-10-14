package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire rule. */
data class TireRuleView(
    var code: String,
    var title: String,
    var dockText: String,
    var category: String,
    var capacity: String,
    var region: String,
    var pallets: String,
    var statusLabel: String,
)

fun toTireRuleView(item: TireRule): TireRuleView {
    return TireRuleView(
        code = item.code,
        title = truncate(item.title, 120),
        dockText = item.dock,
        category = item.category,
        capacity = item.capacity.toString(),
        region = item.region,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireRuleViews(items: MutableList<TireRule>): MutableList<TireRuleView> {
    val views = mutableListOf<TireRuleView>()
    for (item in items) {
        views.add(toTireRuleView(item))
    }
    return views
}

fun sumTireRuleView(view: TireRuleView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedCapacity.toDouble() + parsedPallets.toDouble()
}
