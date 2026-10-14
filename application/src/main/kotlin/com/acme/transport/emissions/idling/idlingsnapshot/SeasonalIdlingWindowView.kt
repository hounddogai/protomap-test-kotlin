package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal idling window. */
data class SeasonalIdlingWindowView(
    var code: String,
    var label: String,
    var locale: String,
    var category: String,
    var notes: String,
    var segment: String,
    var reviewerText: String,
    var fuelLitersText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toSeasonalIdlingWindowView(item: SeasonalIdlingWindow): SeasonalIdlingWindowView {
    return SeasonalIdlingWindowView(
        code = item.code,
        label = truncate(item.label, 80),
        locale = truncate(item.locale, 40),
        category = item.category,
        notes = item.notes,
        segment = item.segment,
        reviewerText = item.reviewer,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalIdlingWindowViews(items: MutableList<SeasonalIdlingWindow>): MutableList<SeasonalIdlingWindowView> {
    val views = mutableListOf<SeasonalIdlingWindowView>()
    for (item in items) {
        views.add(toSeasonalIdlingWindowView(item))
    }
    return views
}

fun sumSeasonalIdlingWindowView(view: SeasonalIdlingWindowView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedFuelLiters + parsedAttempts.toDouble()
}
