package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending scorecard threshold. */
data class PendingScorecardThresholdView(
    var code: String,
    var title: String,
    var channel: String,
    var visibleText: String,
    var label: String,
    var localeText: String,
    var version: String,
    var palletsText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toPendingScorecardThresholdView(item: PendingScorecardThreshold): PendingScorecardThresholdView {
    return PendingScorecardThresholdView(
        code = item.code,
        title = item.title,
        channel = item.channel,
        visibleText = "${item.visible}",
        label = truncate(item.label, 40),
        localeText = item.locale,
        version = item.version.toString(),
        palletsText = item.pallets.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingScorecardThresholdViews(items: MutableList<PendingScorecardThreshold>): MutableList<PendingScorecardThresholdView> {
    val views = mutableListOf<PendingScorecardThresholdView>()
    for (item in items) {
        views.add(toPendingScorecardThresholdView(item))
    }
    return views
}

fun sumPendingScorecardThresholdView(view: PendingScorecardThresholdView): Double {
    val parsedVersion = view.version.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedVersion.toDouble() + parsedPallets.toDouble() + parsedThreshold
}
