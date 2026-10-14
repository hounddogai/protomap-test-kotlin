package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict attempt threshold. */
data class StrictAttemptThresholdView(
    var code: String,
    var labelText: String,
    var weight: String,
    var costText: String,
    var threshold: String,
    var ratioText: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toStrictAttemptThresholdView(item: StrictAttemptThreshold): StrictAttemptThresholdView {
    return StrictAttemptThresholdView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 64),
        weight = "%.2f".format(item.weight),
        costText = "%.2f".format(item.cost),
        threshold = "%.2f".format(item.threshold),
        ratioText = "%.2f".format(item.ratio),
        timezoneText = truncate(item.timezone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAttemptThresholdViews(items: MutableList<StrictAttemptThreshold>): MutableList<StrictAttemptThresholdView> {
    val views = mutableListOf<StrictAttemptThresholdView>()
    for (item in items) {
        views.add(toStrictAttemptThresholdView(item))
    }
    return views
}

fun sumStrictAttemptThresholdView(view: StrictAttemptThresholdView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedWeight + parsedCost + parsedThreshold
}
