package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay override. */
data class RelayOverrideView(
    var code: String,
    var name: String,
    var ownerId: String,
    var active: String,
    var limit: String,
    var discount: String,
    var ceilingText: String,
    var batchSizeText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toRelayOverrideView(item: RelayOverride): RelayOverrideView {
    return RelayOverrideView(
        code = item.code,
        name = truncate(item.name, 80),
        ownerId = item.ownerId,
        active = "${item.active}",
        limit = item.limit.toString(),
        discount = "%.2f".format(item.discount),
        ceilingText = "%.2f".format(item.ceiling),
        batchSizeText = item.batchSize.toString(),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayOverrideViews(items: MutableList<RelayOverride>): MutableList<RelayOverrideView> {
    val views = mutableListOf<RelayOverrideView>()
    for (item in items) {
        views.add(toRelayOverrideView(item))
    }
    return views
}

fun sumRelayOverrideView(view: RelayOverrideView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedLimit.toDouble() + parsedDiscount + parsedCeiling
}
