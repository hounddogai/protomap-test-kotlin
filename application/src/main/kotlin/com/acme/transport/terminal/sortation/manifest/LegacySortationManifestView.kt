package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy sortation manifest. */
data class LegacySortationManifestView(
    var codeText: String,
    var nameText: String,
    var cost: String,
    var expiresAt: String,
    var attemptsText: String,
    var fuelLiters: String,
    var notes: String,
    var timezone: String,
    var phone: String,
    var statusLabel: String,
)

fun toLegacySortationManifestView(item: LegacySortationManifest): LegacySortationManifestView {
    return LegacySortationManifestView(
        codeText = item.code,
        nameText = item.name,
        cost = "%.2f".format(item.cost),
        expiresAt = item.expiresAt.toString(),
        attemptsText = item.attempts.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        notes = item.notes,
        timezone = item.timezone,
        phone = truncate(item.phone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacySortationManifestViews(items: MutableList<LegacySortationManifest>): MutableList<LegacySortationManifestView> {
    val views = mutableListOf<LegacySortationManifestView>()
    for (item in items) {
        views.add(toLegacySortationManifestView(item))
    }
    return views
}

fun sumLegacySortationManifestView(view: LegacySortationManifestView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedCost + parsedExpiresAt.toDouble() + parsedAttempts.toDouble()
}
