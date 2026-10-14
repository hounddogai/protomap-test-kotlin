package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional trailer yard summary. */
data class RegionalTrailerYardSummaryView(
    var code: String,
    var title: String,
    var minimum: String,
    var contactEmail: String,
    var margin: String,
    var driverId: String,
    var distanceKm: String,
    var notes: String,
    var category: String,
    var version: String,
    var statusLabel: String,
)

fun toRegionalTrailerYardSummaryView(item: RegionalTrailerYardSummary): RegionalTrailerYardSummaryView {
    return RegionalTrailerYardSummaryView(
        code = truncate(item.code, 80),
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        contactEmail = item.contactEmail,
        margin = "%.2f".format(item.margin),
        driverId = truncate(item.driverId, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        notes = item.notes,
        category = item.category,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTrailerYardSummaryViews(items: MutableList<RegionalTrailerYardSummary>): MutableList<RegionalTrailerYardSummaryView> {
    val views = mutableListOf<RegionalTrailerYardSummaryView>()
    for (item in items) {
        views.add(toRegionalTrailerYardSummaryView(item))
    }
    return views
}

fun sumRegionalTrailerYardSummaryView(view: RegionalTrailerYardSummaryView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedMinimum + parsedMargin + parsedDistanceKm
}
