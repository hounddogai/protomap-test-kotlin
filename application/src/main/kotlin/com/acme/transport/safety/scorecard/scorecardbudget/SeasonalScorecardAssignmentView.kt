package com.acme.transport.safety.scorecard.scorecardbudget

/** How screens and exports show a seasonal scorecard assignment. */
data class SeasonalScorecardAssignmentView(
    var code: String,
    var label: String,
    var nameText: String,
    var notes: String,
    var countryText: String,
    var routeCode: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toSeasonalScorecardAssignmentView(item: SeasonalScorecardAssignment): SeasonalScorecardAssignmentView {
    return SeasonalScorecardAssignmentView(
        code = item.code,
        label = item.label,
        nameText = item.name,
        notes = item.notes,
        countryText = item.country,
        routeCode = item.routeCode,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalScorecardAssignmentViews(items: MutableList<SeasonalScorecardAssignment>): MutableList<SeasonalScorecardAssignmentView> {
    val views = mutableListOf<SeasonalScorecardAssignmentView>()
    for (item in items) {
        views.add(toSeasonalScorecardAssignmentView(item))
    }
    return views
}

fun sumSeasonalScorecardAssignmentView(view: SeasonalScorecardAssignmentView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedDistanceKm
}
