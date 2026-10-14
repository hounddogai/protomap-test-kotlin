package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy lane plan bundle. */
data class LegacyLanePlanBundleView(
    var code: String,
    var title: String,
    var externalRefText: String,
    var graceDays: String,
    var channelText: String,
    var retriesText: String,
    var laneText: String,
    var vehicleId: String,
    var margin: String,
    var ratio: String,
    var statusLabel: String,
)

fun toLegacyLanePlanBundleView(item: LegacyLanePlanBundle): LegacyLanePlanBundleView {
    return LegacyLanePlanBundleView(
        code = truncate(item.code, 120),
        title = item.title,
        externalRefText = truncate(item.externalRef, 80),
        graceDays = item.graceDays.toString(),
        channelText = item.channel,
        retriesText = item.retries.toString(),
        laneText = truncate(item.lane, 80),
        vehicleId = item.vehicleId,
        margin = "%.2f".format(item.margin),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLanePlanBundleViews(items: MutableList<LegacyLanePlanBundle>): MutableList<LegacyLanePlanBundleView> {
    val views = mutableListOf<LegacyLanePlanBundleView>()
    for (item in items) {
        views.add(toLegacyLanePlanBundleView(item))
    }
    return views
}

fun sumLegacyLanePlanBundleView(view: LegacyLanePlanBundleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedGraceDays.toDouble() + parsedRetries.toDouble() + parsedMargin
}
