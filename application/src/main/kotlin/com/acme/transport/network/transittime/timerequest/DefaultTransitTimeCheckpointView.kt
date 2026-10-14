package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default transit time checkpoint. */
data class DefaultTransitTimeCheckpointView(
    var code: String,
    var nameText: String,
    var pinned: String,
    var ratio: String,
    var trailerId: String,
    var amount: String,
    var threshold: String,
    var locale: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toDefaultTransitTimeCheckpointView(item: DefaultTransitTimeCheckpoint): DefaultTransitTimeCheckpointView {
    return DefaultTransitTimeCheckpointView(
        code = item.code,
        nameText = item.name,
        pinned = "${item.pinned}",
        ratio = "%.2f".format(item.ratio),
        trailerId = truncate(item.trailerId, 40),
        amount = "%.2f".format(item.amount),
        threshold = "%.2f".format(item.threshold),
        locale = item.locale,
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTransitTimeCheckpointViews(items: MutableList<DefaultTransitTimeCheckpoint>): MutableList<DefaultTransitTimeCheckpointView> {
    val views = mutableListOf<DefaultTransitTimeCheckpointView>()
    for (item in items) {
        views.add(toDefaultTransitTimeCheckpointView(item))
    }
    return views
}

fun sumDefaultTransitTimeCheckpointView(view: DefaultTransitTimeCheckpointView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRatio + parsedAmount + parsedThreshold
}
