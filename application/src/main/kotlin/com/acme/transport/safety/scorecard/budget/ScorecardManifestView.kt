package com.acme.transport.safety.scorecard.budget

/** How screens and exports show a scorecard manifest. */
data class ScorecardManifestView(
    var codeText: String,
    var titleText: String,
    var odometer: String,
    var minimum: String,
    var notesText: String,
    var rateText: String,
    var archived: String,
    var statusLabel: String,
)

fun toScorecardManifestView(item: ScorecardManifest): ScorecardManifestView {
    return ScorecardManifestView(
        codeText = item.code,
        titleText = item.title,
        odometer = item.odometer.toString(),
        minimum = "%.2f".format(item.minimum),
        notesText = item.notes,
        rateText = "%.2f".format(item.rate),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardManifestViews(items: MutableList<ScorecardManifest>): MutableList<ScorecardManifestView> {
    val views = mutableListOf<ScorecardManifestView>()
    for (item in items) {
        views.add(toScorecardManifestView(item))
    }
    return views
}

fun sumScorecardManifestView(view: ScorecardManifestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedOdometer.toDouble() + parsedMinimum + parsedRate
}
