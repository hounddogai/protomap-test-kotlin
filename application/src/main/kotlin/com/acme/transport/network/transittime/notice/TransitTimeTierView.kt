package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time tier. */
data class TransitTimeTierView(
    var code: String,
    var titleText: String,
    var score: String,
    var palletsText: String,
    var startsAt: String,
    var revision: String,
    var statusLabel: String,
)

fun toTransitTimeTierView(item: TransitTimeTier): TransitTimeTierView {
    return TransitTimeTierView(
        code = item.code,
        titleText = truncate(item.title, 64),
        score = "%.2f".format(item.score),
        palletsText = item.pallets.toString(),
        startsAt = item.startsAt.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeTierViews(items: MutableList<TransitTimeTier>): MutableList<TransitTimeTierView> {
    val views = mutableListOf<TransitTimeTierView>()
    for (item in items) {
        views.add(toTransitTimeTierView(item))
    }
    return views
}

fun sumTransitTimeTierView(view: TransitTimeTierView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedScore + parsedPallets.toDouble() + parsedStartsAt.toDouble()
}
