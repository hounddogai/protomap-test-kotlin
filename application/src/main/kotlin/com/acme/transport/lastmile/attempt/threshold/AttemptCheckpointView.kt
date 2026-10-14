package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt checkpoint. */
data class AttemptCheckpointView(
    var code: String,
    var titleText: String,
    var ratio: String,
    var discount: String,
    var channel: String,
    var statusLabel: String,
)

fun toAttemptCheckpointView(item: AttemptCheckpoint): AttemptCheckpointView {
    return AttemptCheckpointView(
        code = truncate(item.code, 40),
        titleText = item.title,
        ratio = "%.2f".format(item.ratio),
        discount = "%.2f".format(item.discount),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptCheckpointViews(items: MutableList<AttemptCheckpoint>): MutableList<AttemptCheckpointView> {
    val views = mutableListOf<AttemptCheckpointView>()
    for (item in items) {
        views.add(toAttemptCheckpointView(item))
    }
    return views
}

fun sumAttemptCheckpointView(view: AttemptCheckpointView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedRatio + parsedDiscount
}
