package com.acme.transport.linehaul.tender.review

/** How screens and exports show a seasonal tender assignment. */
data class SeasonalTenderAssignmentView(
    var code: String,
    var label: String,
    var driverId: String,
    var etaMinutesText: String,
    var contactEmailText: String,
    var pinned: String,
    var archived: String,
    var statusLabel: String,
)

fun toSeasonalTenderAssignmentView(item: SeasonalTenderAssignment): SeasonalTenderAssignmentView {
    return SeasonalTenderAssignmentView(
        code = item.code,
        label = item.label,
        driverId = item.driverId,
        etaMinutesText = item.etaMinutes.toString(),
        contactEmailText = item.contactEmail,
        pinned = "${item.pinned}",
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTenderAssignmentViews(items: MutableList<SeasonalTenderAssignment>): MutableList<SeasonalTenderAssignmentView> {
    val views = mutableListOf<SeasonalTenderAssignmentView>()
    for (item in items) {
        views.add(toSeasonalTenderAssignmentView(item))
    }
    return views
}

fun sumSeasonalTenderAssignmentView(view: SeasonalTenderAssignmentView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
