package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global relay variance. */
data class GlobalRelayVarianceView(
    var code: String,
    var label: String,
    var cost: String,
    var score: String,
    var phoneText: String,
    var threshold: String,
    var version: String,
    var notes: String,
    var statusLabel: String,
)

fun toGlobalRelayVarianceView(item: GlobalRelayVariance): GlobalRelayVarianceView {
    return GlobalRelayVarianceView(
        code = item.code,
        label = truncate(item.label, 40),
        cost = "%.2f".format(item.cost),
        score = "%.2f".format(item.score),
        phoneText = item.phone,
        threshold = "%.2f".format(item.threshold),
        version = item.version.toString(),
        notes = truncate(item.notes, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRelayVarianceViews(items: MutableList<GlobalRelayVariance>): MutableList<GlobalRelayVarianceView> {
    val views = mutableListOf<GlobalRelayVarianceView>()
    for (item in items) {
        views.add(toGlobalRelayVarianceView(item))
    }
    return views
}

fun sumGlobalRelayVarianceView(view: GlobalRelayVarianceView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCost + parsedScore + parsedThreshold
}
