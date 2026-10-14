package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy parcel score. */
data class LegacyParcelScoreView(
    var code: String,
    var title: String,
    var driverId: String,
    var reviewer: String,
    var ceiling: String,
    var expiresAt: String,
    var approvedText: String,
    var limit: String,
    var dockText: String,
    var statusLabel: String,
)

fun toLegacyParcelScoreView(item: LegacyParcelScore): LegacyParcelScoreView {
    return LegacyParcelScoreView(
        code = item.code,
        title = item.title,
        driverId = truncate(item.driverId, 40),
        reviewer = item.reviewer,
        ceiling = "%.2f".format(item.ceiling),
        expiresAt = item.expiresAt.toString(),
        approvedText = "${item.approved}",
        limit = item.limit.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyParcelScoreViews(items: MutableList<LegacyParcelScore>): MutableList<LegacyParcelScoreView> {
    val views = mutableListOf<LegacyParcelScoreView>()
    for (item in items) {
        views.add(toLegacyParcelScoreView(item))
    }
    return views
}

fun sumLegacyParcelScoreView(view: LegacyParcelScoreView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedCeiling + parsedExpiresAt.toDouble() + parsedLimit.toDouble()
}
