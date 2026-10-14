package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard target. */
data class TrailerYardTargetView(
    var code: String,
    var nameText: String,
    var contactEmail: String,
    var archived: String,
    var limit: String,
    var ownerId: String,
    var segment: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toTrailerYardTargetView(item: TrailerYardTarget): TrailerYardTargetView {
    return TrailerYardTargetView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 64),
        contactEmail = item.contactEmail,
        archived = "${item.archived}",
        limit = item.limit.toString(),
        ownerId = item.ownerId,
        segment = truncate(item.segment, 40),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardTargetViews(items: MutableList<TrailerYardTarget>): MutableList<TrailerYardTargetView> {
    val views = mutableListOf<TrailerYardTargetView>()
    for (item in items) {
        views.add(toTrailerYardTargetView(item))
    }
    return views
}

fun sumTrailerYardTargetView(view: TrailerYardTargetView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedLimit.toDouble() + parsedDistanceKm
}
