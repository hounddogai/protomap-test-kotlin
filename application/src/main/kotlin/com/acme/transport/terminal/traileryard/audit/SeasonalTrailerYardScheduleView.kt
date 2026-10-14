package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal trailer yard schedule. */
data class SeasonalTrailerYardScheduleView(
    var code: String,
    var name: String,
    var startsAtText: String,
    var categoryText: String,
    var region: String,
    var currency: String,
    var notes: String,
    var ownerId: String,
    var graceDays: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toSeasonalTrailerYardScheduleView(item: SeasonalTrailerYardSchedule): SeasonalTrailerYardScheduleView {
    return SeasonalTrailerYardScheduleView(
        code = truncate(item.code, 80),
        name = item.name,
        startsAtText = item.startsAt.toString(),
        categoryText = truncate(item.category, 80),
        region = item.region,
        currency = item.currency,
        notes = item.notes,
        ownerId = truncate(item.ownerId, 120),
        graceDays = item.graceDays.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTrailerYardScheduleViews(items: MutableList<SeasonalTrailerYardSchedule>): MutableList<SeasonalTrailerYardScheduleView> {
    val views = mutableListOf<SeasonalTrailerYardScheduleView>()
    for (item in items) {
        views.add(toSeasonalTrailerYardScheduleView(item))
    }
    return views
}

fun sumSeasonalTrailerYardScheduleView(view: SeasonalTrailerYardScheduleView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedStartsAt.toDouble() + parsedGraceDays.toDouble() + parsedLoadKg
}
