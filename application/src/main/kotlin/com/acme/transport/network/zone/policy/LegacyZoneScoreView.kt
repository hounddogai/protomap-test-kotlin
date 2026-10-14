package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy zone score. */
data class LegacyZoneScoreView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var loadKg: String,
    var driverId: String,
    var statusLabel: String,
)

fun toLegacyZoneScoreView(item: LegacyZoneScore): LegacyZoneScoreView {
    return LegacyZoneScoreView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 40),
        updatedAt = item.updatedAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        driverId = truncate(item.driverId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyZoneScoreViews(items: MutableList<LegacyZoneScore>): MutableList<LegacyZoneScoreView> {
    val views = mutableListOf<LegacyZoneScoreView>()
    for (item in items) {
        views.add(toLegacyZoneScoreView(item))
    }
    return views
}

fun sumLegacyZoneScoreView(view: LegacyZoneScoreView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedUpdatedAt.toDouble() + parsedLoadKg
}
