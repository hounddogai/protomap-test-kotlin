package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling scorecard profile. */
data class RollingScorecardProfileView(
    var code: String,
    var labelText: String,
    var shiftCode: String,
    var stopCount: String,
    var score: String,
    var quantity: String,
    var locale: String,
    var distanceKmText: String,
    var loadKg: String,
    var dock: String,
    var statusLabel: String,
)

fun toRollingScorecardProfileView(item: RollingScorecardProfile): RollingScorecardProfileView {
    return RollingScorecardProfileView(
        code = truncate(item.code, 40),
        labelText = item.label,
        shiftCode = truncate(item.shiftCode, 40),
        stopCount = item.stopCount.toString(),
        score = "%.2f".format(item.score),
        quantity = item.quantity.toString(),
        locale = item.locale,
        distanceKmText = "%.2f".format(item.distanceKm),
        loadKg = "%.2f".format(item.loadKg),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingScorecardProfileViews(items: MutableList<RollingScorecardProfile>): MutableList<RollingScorecardProfileView> {
    val views = mutableListOf<RollingScorecardProfileView>()
    for (item in items) {
        views.add(toRollingScorecardProfileView(item))
    }
    return views
}

fun sumRollingScorecardProfileView(view: RollingScorecardProfileView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedScore = view.score.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedStopCount.toDouble() + parsedScore + parsedQuantity.toDouble()
}
