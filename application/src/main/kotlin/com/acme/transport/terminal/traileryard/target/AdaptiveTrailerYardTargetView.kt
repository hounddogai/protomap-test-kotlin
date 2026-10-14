package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive trailer yard target. */
data class AdaptiveTrailerYardTargetView(
    var code: String,
    var titleText: String,
    var externalRef: String,
    var startsAt: String,
    var locale: String,
    var ratioText: String,
    var minimum: String,
    var priority: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toAdaptiveTrailerYardTargetView(item: AdaptiveTrailerYardTarget): AdaptiveTrailerYardTargetView {
    return AdaptiveTrailerYardTargetView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 80),
        externalRef = item.externalRef,
        startsAt = item.startsAt.toString(),
        locale = item.locale,
        ratioText = "%.2f".format(item.ratio),
        minimum = "%.2f".format(item.minimum),
        priority = item.priority.toString(),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTrailerYardTargetViews(items: MutableList<AdaptiveTrailerYardTarget>): MutableList<AdaptiveTrailerYardTargetView> {
    val views = mutableListOf<AdaptiveTrailerYardTargetView>()
    for (item in items) {
        views.add(toAdaptiveTrailerYardTargetView(item))
    }
    return views
}

fun sumAdaptiveTrailerYardTargetView(view: AdaptiveTrailerYardTargetView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStartsAt.toDouble() + parsedRatio + parsedMinimum
}
