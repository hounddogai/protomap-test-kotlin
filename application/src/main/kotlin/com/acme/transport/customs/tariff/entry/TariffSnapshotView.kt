package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff snapshot. */
data class TariffSnapshotView(
    var code: String,
    var nameText: String,
    var routeCodeText: String,
    var trailerId: String,
    var reviewer: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toTariffSnapshotView(item: TariffSnapshot): TariffSnapshotView {
    return TariffSnapshotView(
        code = item.code,
        nameText = item.name,
        routeCodeText = item.routeCode,
        trailerId = item.trailerId,
        reviewer = truncate(item.reviewer, 64),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffSnapshotViews(items: MutableList<TariffSnapshot>): MutableList<TariffSnapshotView> {
    val views = mutableListOf<TariffSnapshotView>()
    for (item in items) {
        views.add(toTariffSnapshotView(item))
    }
    return views
}

fun sumTariffSnapshotView(view: TariffSnapshotView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
