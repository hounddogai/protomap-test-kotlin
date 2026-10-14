package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard manifest. */
data class TrailerYardManifestView(
    var code: String,
    var title: String,
    var graceDays: String,
    var priority: String,
    var trailerId: String,
    var costText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toTrailerYardManifestView(item: TrailerYardManifest): TrailerYardManifestView {
    return TrailerYardManifestView(
        code = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        priority = item.priority.toString(),
        trailerId = truncate(item.trailerId, 40),
        costText = "%.2f".format(item.cost),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardManifestViews(items: MutableList<TrailerYardManifest>): MutableList<TrailerYardManifestView> {
    val views = mutableListOf<TrailerYardManifestView>()
    for (item in items) {
        views.add(toTrailerYardManifestView(item))
    }
    return views
}

fun sumTrailerYardManifestView(view: TrailerYardManifestView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedGraceDays.toDouble() + parsedPriority.toDouble() + parsedCost
}
