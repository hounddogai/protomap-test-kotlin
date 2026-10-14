package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback broker bundle. */
data class FallbackBrokerBundleView(
    var code: String,
    var nameText: String,
    var minimum: String,
    var visible: String,
    var country: String,
    var durationMinutesText: String,
    var graceDays: String,
    var sequence: String,
    var loadKgText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toFallbackBrokerBundleView(item: FallbackBrokerBundle): FallbackBrokerBundleView {
    return FallbackBrokerBundleView(
        code = truncate(item.code, 120),
        nameText = item.name,
        minimum = "%.2f".format(item.minimum),
        visible = "${item.visible}",
        country = item.country,
        durationMinutesText = item.durationMinutes.toString(),
        graceDays = item.graceDays.toString(),
        sequence = item.sequence.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackBrokerBundleViews(items: MutableList<FallbackBrokerBundle>): MutableList<FallbackBrokerBundleView> {
    val views = mutableListOf<FallbackBrokerBundleView>()
    for (item in items) {
        views.add(toFallbackBrokerBundleView(item))
    }
    return views
}

fun sumFallbackBrokerBundleView(view: FallbackBrokerBundleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMinimum + parsedDurationMinutes.toDouble() + parsedGraceDays.toDouble()
}
