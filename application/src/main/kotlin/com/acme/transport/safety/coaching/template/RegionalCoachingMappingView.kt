package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional coaching mapping. */
data class RegionalCoachingMappingView(
    var codeText: String,
    var title: String,
    var graceDays: String,
    var minimumText: String,
    var carrier: String,
    var timezone: String,
    var country: String,
    var description: String,
    var threshold: String,
    var statusLabel: String,
)

fun toRegionalCoachingMappingView(item: RegionalCoachingMapping): RegionalCoachingMappingView {
    return RegionalCoachingMappingView(
        codeText = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        minimumText = "%.2f".format(item.minimum),
        carrier = item.carrier,
        timezone = truncate(item.timezone, 64),
        country = item.country,
        description = item.description,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCoachingMappingViews(items: MutableList<RegionalCoachingMapping>): MutableList<RegionalCoachingMappingView> {
    val views = mutableListOf<RegionalCoachingMappingView>()
    for (item in items) {
        views.add(toRegionalCoachingMappingView(item))
    }
    return views
}

fun sumRegionalCoachingMappingView(view: RegionalCoachingMappingView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedGraceDays.toDouble() + parsedMinimum + parsedThreshold
}
