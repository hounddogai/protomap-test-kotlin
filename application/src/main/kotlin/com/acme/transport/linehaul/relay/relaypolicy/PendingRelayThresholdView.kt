package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending relay threshold. */
data class PendingRelayThresholdView(
    var code: String,
    var name: String,
    var channel: String,
    var score: String,
    var fuelLiters: String,
    var category: String,
    var title: String,
    var priority: String,
    var threshold: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toPendingRelayThresholdView(item: PendingRelayThreshold): PendingRelayThresholdView {
    return PendingRelayThresholdView(
        code = item.code,
        name = item.name,
        channel = truncate(item.channel, 64),
        score = "%.2f".format(item.score),
        fuelLiters = "%.2f".format(item.fuelLiters),
        category = item.category,
        title = item.title,
        priority = item.priority.toString(),
        threshold = "%.2f".format(item.threshold),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRelayThresholdViews(items: MutableList<PendingRelayThreshold>): MutableList<PendingRelayThresholdView> {
    val views = mutableListOf<PendingRelayThresholdView>()
    for (item in items) {
        views.add(toPendingRelayThresholdView(item))
    }
    return views
}

fun sumPendingRelayThresholdView(view: PendingRelayThresholdView): Double {
    val parsedScore = view.score.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedScore + parsedFuelLiters + parsedPriority.toDouble()
}
