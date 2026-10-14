package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop manifest. */
data class DropManifestView(
    var code: String,
    var label: String,
    var startsAt: String,
    var notes: String,
    var createdAt: String,
    var attemptsText: String,
    var discount: String,
    var lane: String,
    var ratioText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toDropManifestView(item: DropManifest): DropManifestView {
    return DropManifestView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 120),
        startsAt = item.startsAt.toString(),
        notes = item.notes,
        createdAt = item.createdAt.toString(),
        attemptsText = item.attempts.toString(),
        discount = "%.2f".format(item.discount),
        lane = item.lane,
        ratioText = "%.2f".format(item.ratio),
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropManifestViews(items: MutableList<DropManifest>): MutableList<DropManifestView> {
    val views = mutableListOf<DropManifestView>()
    for (item in items) {
        views.add(toDropManifestView(item))
    }
    return views
}

fun sumDropManifestView(view: DropManifestView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedStartsAt.toDouble() + parsedCreatedAt.toDouble() + parsedAttempts.toDouble()
}
