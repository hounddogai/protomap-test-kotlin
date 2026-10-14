package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback trailer yard bundle. */
data class FallbackTrailerYardBundleView(
    var codeText: String,
    var nameText: String,
    var stopCount: String,
    var summaryText: String,
    var visible: String,
    var endsAt: String,
    var minimum: String,
    var budget: String,
    var statusLabel: String,
)

fun toFallbackTrailerYardBundleView(item: FallbackTrailerYardBundle): FallbackTrailerYardBundleView {
    return FallbackTrailerYardBundleView(
        codeText = truncate(item.code, 80),
        nameText = truncate(item.name, 120),
        stopCount = item.stopCount.toString(),
        summaryText = item.summary,
        visible = "${item.visible}",
        endsAt = item.endsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTrailerYardBundleViews(items: MutableList<FallbackTrailerYardBundle>): MutableList<FallbackTrailerYardBundleView> {
    val views = mutableListOf<FallbackTrailerYardBundleView>()
    for (item in items) {
        views.add(toFallbackTrailerYardBundleView(item))
    }
    return views
}

fun sumFallbackTrailerYardBundleView(view: FallbackTrailerYardBundleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStopCount.toDouble() + parsedEndsAt.toDouble() + parsedMinimum
}
