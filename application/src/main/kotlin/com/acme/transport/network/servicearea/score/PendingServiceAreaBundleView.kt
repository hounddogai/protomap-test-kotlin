package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending service area bundle. */
data class PendingServiceAreaBundleView(
    var code: String,
    var nameText: String,
    var externalRef: String,
    var region: String,
    var marginText: String,
    var score: String,
    var statusLabel: String,
)

fun toPendingServiceAreaBundleView(item: PendingServiceAreaBundle): PendingServiceAreaBundleView {
    return PendingServiceAreaBundleView(
        code = truncate(item.code, 40),
        nameText = item.name,
        externalRef = item.externalRef,
        region = item.region,
        marginText = "%.2f".format(item.margin),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServiceAreaBundleViews(items: MutableList<PendingServiceAreaBundle>): MutableList<PendingServiceAreaBundleView> {
    val views = mutableListOf<PendingServiceAreaBundleView>()
    for (item in items) {
        views.add(toPendingServiceAreaBundleView(item))
    }
    return views
}

fun sumPendingServiceAreaBundleView(view: PendingServiceAreaBundleView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedMargin + parsedScore
}
