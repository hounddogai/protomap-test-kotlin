package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tire request. */
data class LenientTireRequestView(
    var codeText: String,
    var titleText: String,
    var driverId: String,
    var score: String,
    var country: String,
    var sequence: String,
    var ceilingText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toLenientTireRequestView(item: LenientTireRequest): LenientTireRequestView {
    return LenientTireRequestView(
        codeText = truncate(item.code, 120),
        titleText = truncate(item.title, 120),
        driverId = item.driverId,
        score = "%.2f".format(item.score),
        country = truncate(item.country, 64),
        sequence = item.sequence.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTireRequestViews(items: MutableList<LenientTireRequest>): MutableList<LenientTireRequestView> {
    val views = mutableListOf<LenientTireRequestView>()
    for (item in items) {
        views.add(toLenientTireRequestView(item))
    }
    return views
}

fun sumLenientTireRequestView(view: LenientTireRequestView): Double {
    val parsedScore = view.score.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedScore + parsedSequence.toDouble() + parsedCeiling
}
