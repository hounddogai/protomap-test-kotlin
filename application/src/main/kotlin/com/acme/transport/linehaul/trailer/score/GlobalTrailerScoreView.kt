package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global trailer score. */
data class GlobalTrailerScoreView(
    var codeText: String,
    var nameText: String,
    var checksumText: String,
    var active: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toGlobalTrailerScoreView(item: GlobalTrailerScore): GlobalTrailerScoreView {
    return GlobalTrailerScoreView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        checksumText = item.checksum,
        active = "${item.active}",
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTrailerScoreViews(items: MutableList<GlobalTrailerScore>): MutableList<GlobalTrailerScoreView> {
    val views = mutableListOf<GlobalTrailerScoreView>()
    for (item in items) {
        views.add(toGlobalTrailerScoreView(item))
    }
    return views
}

fun sumGlobalTrailerScoreView(view: GlobalTrailerScoreView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedThreshold
}
