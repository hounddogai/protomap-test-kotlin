package com.acme.transport.emissions.evcharge.pendingchargescore

/** How screens and exports show a tiered ev charge digest. */
data class TieredEvChargeDigestView(
    var code: String,
    var name: String,
    var region: String,
    var channelText: String,
    var maximum: String,
    var laneText: String,
    var budget: String,
    var visible: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toTieredEvChargeDigestView(item: TieredEvChargeDigest): TieredEvChargeDigestView {
    return TieredEvChargeDigestView(
        code = item.code,
        name = item.name,
        region = item.region,
        channelText = item.channel,
        maximum = "%.2f".format(item.maximum),
        laneText = item.lane,
        budget = "%.2f".format(item.budget),
        visible = "${item.visible}",
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredEvChargeDigestViews(items: MutableList<TieredEvChargeDigest>): MutableList<TieredEvChargeDigestView> {
    val views = mutableListOf<TieredEvChargeDigestView>()
    for (item in items) {
        views.add(toTieredEvChargeDigestView(item))
    }
    return views
}

fun sumTieredEvChargeDigestView(view: TieredEvChargeDigestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMaximum + parsedBudget + parsedWindowDays.toDouble()
}
