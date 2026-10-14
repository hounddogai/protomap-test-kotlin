package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered node bundle. */
data class TieredNodeBundleView(
    var codeText: String,
    var nameText: String,
    var rate: String,
    var expiresAtText: String,
    var lane: String,
    var segment: String,
    var scoreText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toTieredNodeBundleView(item: TieredNodeBundle): TieredNodeBundleView {
    return TieredNodeBundleView(
        codeText = item.code,
        nameText = truncate(item.name, 64),
        rate = "%.2f".format(item.rate),
        expiresAtText = item.expiresAt.toString(),
        lane = item.lane,
        segment = item.segment,
        scoreText = "%.2f".format(item.score),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredNodeBundleViews(items: MutableList<TieredNodeBundle>): MutableList<TieredNodeBundleView> {
    val views = mutableListOf<TieredNodeBundleView>()
    for (item in items) {
        views.add(toTieredNodeBundleView(item))
    }
    return views
}

fun sumTieredNodeBundleView(view: TieredNodeBundleView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedRate + parsedExpiresAt.toDouble() + parsedScore
}
