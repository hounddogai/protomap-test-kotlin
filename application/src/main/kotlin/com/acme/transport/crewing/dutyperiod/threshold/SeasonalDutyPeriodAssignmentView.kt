package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal duty period assignment. */
data class SeasonalDutyPeriodAssignmentView(
    var code: String,
    var name: String,
    var title: String,
    var etaMinutesText: String,
    var fuelLiters: String,
    var scoreText: String,
    var notes: String,
    var statusLabel: String,
)

fun toSeasonalDutyPeriodAssignmentView(item: SeasonalDutyPeriodAssignment): SeasonalDutyPeriodAssignmentView {
    return SeasonalDutyPeriodAssignmentView(
        code = truncate(item.code, 64),
        name = item.name,
        title = item.title,
        etaMinutesText = item.etaMinutes.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        scoreText = "%.2f".format(item.score),
        notes = truncate(item.notes, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDutyPeriodAssignmentViews(items: MutableList<SeasonalDutyPeriodAssignment>): MutableList<SeasonalDutyPeriodAssignmentView> {
    val views = mutableListOf<SeasonalDutyPeriodAssignmentView>()
    for (item in items) {
        views.add(toSeasonalDutyPeriodAssignmentView(item))
    }
    return views
}

fun sumSeasonalDutyPeriodAssignmentView(view: SeasonalDutyPeriodAssignmentView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedFuelLiters + parsedScore
}
