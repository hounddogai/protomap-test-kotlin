package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster variance. */
data class RosterVarianceView(
    var code: String,
    var label: String,
    var threshold: String,
    var graceDaysText: String,
    var endsAtText: String,
    var externalRefText: String,
    var reviewer: String,
    var driverId: String,
    var ratio: String,
    var channel: String,
    var statusLabel: String,
)

fun toRosterVarianceView(item: RosterVariance): RosterVarianceView {
    return RosterVarianceView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 64),
        threshold = "%.2f".format(item.threshold),
        graceDaysText = item.graceDays.toString(),
        endsAtText = item.endsAt.toString(),
        externalRefText = item.externalRef,
        reviewer = item.reviewer,
        driverId = truncate(item.driverId, 40),
        ratio = "%.2f".format(item.ratio),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterVarianceViews(items: MutableList<RosterVariance>): MutableList<RosterVarianceView> {
    val views = mutableListOf<RosterVarianceView>()
    for (item in items) {
        views.add(toRosterVarianceView(item))
    }
    return views
}

fun sumRosterVarianceView(view: RosterVarianceView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedThreshold + parsedGraceDays.toDouble() + parsedEndsAt.toDouble()
}
