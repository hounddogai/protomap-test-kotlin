package com.acme.transport.network.transittime.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time score. */
data class TransitTimeScoreView(
    var code: String,
    var titleText: String,
    var channel: String,
    var ratio: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTransitTimeScoreView(item: TransitTimeScore): TransitTimeScoreView {
    return TransitTimeScoreView(
        code = truncate(item.code, 40),
        titleText = item.title,
        channel = item.channel,
        ratio = "%.2f".format(item.ratio),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeScoreViews(items: MutableList<TransitTimeScore>): MutableList<TransitTimeScoreView> {
    val views = mutableListOf<TransitTimeScoreView>()
    for (item in items) {
        views.add(toTransitTimeScoreView(item))
    }
    return views
}

fun sumTransitTimeScoreView(view: TransitTimeScoreView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedRatio + parsedCapacity.toDouble()
}
