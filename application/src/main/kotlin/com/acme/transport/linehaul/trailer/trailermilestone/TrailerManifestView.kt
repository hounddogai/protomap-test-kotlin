package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer manifest. */
data class TrailerManifestView(
    var code: String,
    var titleText: String,
    var etaMinutesText: String,
    var ceiling: String,
    var archived: String,
    var fuelLiters: String,
    var revision: String,
    var descriptionText: String,
    var summary: String,
    var statusLabel: String,
)

fun toTrailerManifestView(item: TrailerManifest): TrailerManifestView {
    return TrailerManifestView(
        code = item.code,
        titleText = item.title,
        etaMinutesText = item.etaMinutes.toString(),
        ceiling = "%.2f".format(item.ceiling),
        archived = "${item.archived}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        revision = item.revision.toString(),
        descriptionText = item.description,
        summary = truncate(item.summary, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerManifestViews(items: MutableList<TrailerManifest>): MutableList<TrailerManifestView> {
    val views = mutableListOf<TrailerManifestView>()
    for (item in items) {
        views.add(toTrailerManifestView(item))
    }
    return views
}

fun sumTrailerManifestView(view: TrailerManifestView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedEtaMinutes.toDouble() + parsedCeiling + parsedFuelLiters
}
