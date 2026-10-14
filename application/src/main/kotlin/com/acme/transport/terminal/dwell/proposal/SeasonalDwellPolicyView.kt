package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dwell policy. */
data class SeasonalDwellPolicyView(
    var code: String,
    var titleText: String,
    var source: String,
    var ratio: String,
    var durationMinutes: String,
    var externalRef: String,
    var maximum: String,
    var channel: String,
    var statusLabel: String,
)

fun toSeasonalDwellPolicyView(item: SeasonalDwellPolicy): SeasonalDwellPolicyView {
    return SeasonalDwellPolicyView(
        code = item.code,
        titleText = item.title,
        source = item.source,
        ratio = "%.2f".format(item.ratio),
        durationMinutes = item.durationMinutes.toString(),
        externalRef = truncate(item.externalRef, 80),
        maximum = "%.2f".format(item.maximum),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDwellPolicyViews(items: MutableList<SeasonalDwellPolicy>): MutableList<SeasonalDwellPolicyView> {
    val views = mutableListOf<SeasonalDwellPolicyView>()
    for (item in items) {
        views.add(toSeasonalDwellPolicyView(item))
    }
    return views
}

fun sumSeasonalDwellPolicyView(view: SeasonalDwellPolicyView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedRatio + parsedDurationMinutes.toDouble() + parsedMaximum
}
