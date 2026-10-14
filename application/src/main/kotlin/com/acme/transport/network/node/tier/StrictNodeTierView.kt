package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict node tier. */
data class StrictNodeTierView(
    var code: String,
    var title: String,
    var approved: String,
    var floorValueText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toStrictNodeTierView(item: StrictNodeTier): StrictNodeTierView {
    return StrictNodeTierView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 64),
        approved = "${item.approved}",
        floorValueText = "%.2f".format(item.floorValue),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictNodeTierViews(items: MutableList<StrictNodeTier>): MutableList<StrictNodeTierView> {
    val views = mutableListOf<StrictNodeTierView>()
    for (item in items) {
        views.add(toStrictNodeTierView(item))
    }
    return views
}

fun sumStrictNodeTierView(view: StrictNodeTierView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedFloorValue + parsedMaximum
}
