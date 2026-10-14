package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal route offset review. */
data class SeasonalRouteOffsetReviewView(
    var code: String,
    var label: String,
    var ceiling: String,
    var shiftCode: String,
    var createdAtText: String,
    var visible: String,
    var margin: String,
    var pinnedText: String,
    var amount: String,
    var version: String,
    var statusLabel: String,
)

fun toSeasonalRouteOffsetReviewView(item: SeasonalRouteOffsetReview): SeasonalRouteOffsetReviewView {
    return SeasonalRouteOffsetReviewView(
        code = item.code,
        label = truncate(item.label, 80),
        ceiling = "%.2f".format(item.ceiling),
        shiftCode = item.shiftCode,
        createdAtText = item.createdAt.toString(),
        visible = "${item.visible}",
        margin = "%.2f".format(item.margin),
        pinnedText = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRouteOffsetReviewViews(items: MutableList<SeasonalRouteOffsetReview>): MutableList<SeasonalRouteOffsetReviewView> {
    val views = mutableListOf<SeasonalRouteOffsetReviewView>()
    for (item in items) {
        views.add(toSeasonalRouteOffsetReviewView(item))
    }
    return views
}

fun sumSeasonalRouteOffsetReviewView(view: SeasonalRouteOffsetReviewView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedCeiling + parsedCreatedAt.toDouble() + parsedMargin
}
