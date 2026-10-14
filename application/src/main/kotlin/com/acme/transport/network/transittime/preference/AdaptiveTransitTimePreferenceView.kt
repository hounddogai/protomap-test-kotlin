package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive transit time preference. */
data class AdaptiveTransitTimePreferenceView(
    var codeText: String,
    var label: String,
    var maximum: String,
    var sourceText: String,
    var loadKg: String,
    var unitText: String,
    var statusLabel: String,
)

fun toAdaptiveTransitTimePreferenceView(item: AdaptiveTransitTimePreference): AdaptiveTransitTimePreferenceView {
    return AdaptiveTransitTimePreferenceView(
        codeText = item.code,
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        sourceText = item.source,
        loadKg = "%.2f".format(item.loadKg),
        unitText = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTransitTimePreferenceViews(items: MutableList<AdaptiveTransitTimePreference>): MutableList<AdaptiveTransitTimePreferenceView> {
    val views = mutableListOf<AdaptiveTransitTimePreferenceView>()
    for (item in items) {
        views.add(toAdaptiveTransitTimePreferenceView(item))
    }
    return views
}

fun sumAdaptiveTransitTimePreferenceView(view: AdaptiveTransitTimePreferenceView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedMaximum + parsedLoadKg
}
