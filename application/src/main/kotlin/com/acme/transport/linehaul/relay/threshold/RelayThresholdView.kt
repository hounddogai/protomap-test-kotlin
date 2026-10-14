package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay threshold. */
data class RelayThresholdView(
    var codeText: String,
    var title: String,
    var minimum: String,
    var description: String,
    var routeCode: String,
    var score: String,
    var statusLabel: String,
)

fun toRelayThresholdView(item: RelayThreshold): RelayThresholdView {
    return RelayThresholdView(
        codeText = item.code,
        title = truncate(item.title, 40),
        minimum = "%.2f".format(item.minimum),
        description = item.description,
        routeCode = item.routeCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayThresholdViews(items: MutableList<RelayThreshold>): MutableList<RelayThresholdView> {
    val views = mutableListOf<RelayThresholdView>()
    for (item in items) {
        views.add(toRelayThresholdView(item))
    }
    return views
}

fun sumRelayThresholdView(view: RelayThresholdView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedMinimum + parsedScore
}
