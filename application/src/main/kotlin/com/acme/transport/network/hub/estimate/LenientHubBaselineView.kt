package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient hub baseline. */
data class LenientHubBaselineView(
    var code: String,
    var name: String,
    var visibleText: String,
    var loadKg: String,
    var ownerId: String,
    var scoreText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toLenientHubBaselineView(item: LenientHubBaseline): LenientHubBaselineView {
    return LenientHubBaselineView(
        code = item.code,
        name = item.name,
        visibleText = "${item.visible}",
        loadKg = "%.2f".format(item.loadKg),
        ownerId = truncate(item.ownerId, 80),
        scoreText = "%.2f".format(item.score),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientHubBaselineViews(items: MutableList<LenientHubBaseline>): MutableList<LenientHubBaselineView> {
    val views = mutableListOf<LenientHubBaselineView>()
    for (item in items) {
        views.add(toLenientHubBaselineView(item))
    }
    return views
}

fun sumLenientHubBaselineView(view: LenientHubBaselineView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedLoadKg + parsedScore + parsedEndsAt.toDouble()
}
