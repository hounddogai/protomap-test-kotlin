package com.acme.transport.network.node.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped node variance. */
data class CappedNodeVarianceView(
    var codeText: String,
    var nameText: String,
    var retries: String,
    var active: String,
    var notes: String,
    var visible: String,
    var threshold: String,
    var score: String,
    var pallets: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toCappedNodeVarianceView(item: CappedNodeVariance): CappedNodeVarianceView {
    return CappedNodeVarianceView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        retries = item.retries.toString(),
        active = "${item.active}",
        notes = truncate(item.notes, 64),
        visible = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        score = "%.2f".format(item.score),
        pallets = item.pallets.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedNodeVarianceViews(items: MutableList<CappedNodeVariance>): MutableList<CappedNodeVarianceView> {
    val views = mutableListOf<CappedNodeVarianceView>()
    for (item in items) {
        views.add(toCappedNodeVarianceView(item))
    }
    return views
}

fun sumCappedNodeVarianceView(view: CappedNodeVarianceView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRetries.toDouble() + parsedThreshold + parsedScore
}
