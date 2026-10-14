package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel baseline. */
data class ParcelBaselineView(
    var code: String,
    var label: String,
    var channelText: String,
    var category: String,
    var routeCodeText: String,
    var score: String,
    var statusLabel: String,
)

fun toParcelBaselineView(item: ParcelBaseline): ParcelBaselineView {
    return ParcelBaselineView(
        code = item.code,
        label = truncate(item.label, 64),
        channelText = item.channel,
        category = item.category,
        routeCodeText = truncate(item.routeCode, 120),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelBaselineViews(items: MutableList<ParcelBaseline>): MutableList<ParcelBaselineView> {
    val views = mutableListOf<ParcelBaselineView>()
    for (item in items) {
        views.add(toParcelBaselineView(item))
    }
    return views
}

fun sumParcelBaselineView(view: ParcelBaselineView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
