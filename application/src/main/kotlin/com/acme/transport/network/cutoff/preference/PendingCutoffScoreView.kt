package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending cutoff score. */
data class PendingCutoffScoreView(
    var code: String,
    var labelText: String,
    var updatedAtText: String,
    var contactEmail: String,
    var locale: String,
    var rate: String,
    var name: String,
    var ownerId: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toPendingCutoffScoreView(item: PendingCutoffScore): PendingCutoffScoreView {
    return PendingCutoffScoreView(
        code = item.code,
        labelText = item.label,
        updatedAtText = item.updatedAt.toString(),
        contactEmail = item.contactEmail,
        locale = item.locale,
        rate = "%.2f".format(item.rate),
        name = truncate(item.name, 120),
        ownerId = truncate(item.ownerId, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCutoffScoreViews(items: MutableList<PendingCutoffScore>): MutableList<PendingCutoffScoreView> {
    val views = mutableListOf<PendingCutoffScoreView>()
    for (item in items) {
        views.add(toPendingCutoffScoreView(item))
    }
    return views
}

fun sumPendingCutoffScoreView(view: PendingCutoffScoreView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedUpdatedAt.toDouble() + parsedRate + parsedDistanceKm
}
