package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary attempt digest. */
data class PrimaryAttemptDigestView(
    var code: String,
    var label: String,
    var visible: String,
    var threshold: String,
    var approved: String,
    var fuelLitersText: String,
    var expiresAt: String,
    var nameText: String,
    var costText: String,
    var dock: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toPrimaryAttemptDigestView(item: PrimaryAttemptDigest): PrimaryAttemptDigestView {
    return PrimaryAttemptDigestView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 40),
        visible = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        approved = "${item.approved}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        expiresAt = item.expiresAt.toString(),
        nameText = item.name,
        costText = "%.2f".format(item.cost),
        dock = item.dock,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAttemptDigestViews(items: MutableList<PrimaryAttemptDigest>): MutableList<PrimaryAttemptDigestView> {
    val views = mutableListOf<PrimaryAttemptDigestView>()
    for (item in items) {
        views.add(toPrimaryAttemptDigestView(item))
    }
    return views
}

fun sumPrimaryAttemptDigestView(view: PrimaryAttemptDigestView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedThreshold + parsedFuelLiters + parsedExpiresAt.toDouble()
}
