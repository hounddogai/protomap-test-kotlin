package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal trailer yard score. */
data class SeasonalTrailerYardScoreView(
    var code: String,
    var title: String,
    var startsAt: String,
    var driverId: String,
    var category: String,
    var attempts: String,
    var checksum: String,
    var trailerIdText: String,
    var contactEmailText: String,
    var segment: String,
    var cost: String,
    var statusLabel: String,
)

fun toSeasonalTrailerYardScoreView(item: SeasonalTrailerYardScore): SeasonalTrailerYardScoreView {
    return SeasonalTrailerYardScoreView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 64),
        startsAt = item.startsAt.toString(),
        driverId = item.driverId,
        category = truncate(item.category, 80),
        attempts = item.attempts.toString(),
        checksum = item.checksum,
        trailerIdText = item.trailerId,
        contactEmailText = item.contactEmail,
        segment = item.segment,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTrailerYardScoreViews(items: MutableList<SeasonalTrailerYardScore>): MutableList<SeasonalTrailerYardScoreView> {
    val views = mutableListOf<SeasonalTrailerYardScoreView>()
    for (item in items) {
        views.add(toSeasonalTrailerYardScoreView(item))
    }
    return views
}

fun sumSeasonalTrailerYardScoreView(view: SeasonalTrailerYardScoreView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedAttempts.toDouble() + parsedCost
}
