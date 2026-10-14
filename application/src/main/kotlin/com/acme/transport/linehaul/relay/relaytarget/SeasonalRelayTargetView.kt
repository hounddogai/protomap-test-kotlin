package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal relay target. */
data class SeasonalRelayTargetView(
    var code: String,
    var title: String,
    var threshold: String,
    var quantity: String,
    var dock: String,
    var version: String,
    var statusLabel: String,
)

fun toSeasonalRelayTargetView(item: SeasonalRelayTarget): SeasonalRelayTargetView {
    return SeasonalRelayTargetView(
        code = item.code,
        title = truncate(item.title, 40),
        threshold = "%.2f".format(item.threshold),
        quantity = item.quantity.toString(),
        dock = item.dock,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRelayTargetViews(items: MutableList<SeasonalRelayTarget>): MutableList<SeasonalRelayTargetView> {
    val views = mutableListOf<SeasonalRelayTargetView>()
    for (item in items) {
        views.add(toSeasonalRelayTargetView(item))
    }
    return views
}

fun sumSeasonalRelayTargetView(view: SeasonalRelayTargetView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedVersion = view.version.toInt()
    return parsedThreshold + parsedQuantity.toDouble() + parsedVersion.toDouble()
}
