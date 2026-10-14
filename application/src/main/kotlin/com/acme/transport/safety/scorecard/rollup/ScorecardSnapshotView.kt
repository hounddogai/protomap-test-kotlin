package com.acme.transport.safety.scorecard.rollup

/** How screens and exports show a scorecard snapshot. */
data class ScorecardSnapshotView(
    var code: String,
    var labelText: String,
    var limit: String,
    var summaryText: String,
    var active: String,
    var distanceKmText: String,
    var ceiling: String,
    var ownerId: String,
    var windowDaysText: String,
    var checksum: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toScorecardSnapshotView(item: ScorecardSnapshot): ScorecardSnapshotView {
    return ScorecardSnapshotView(
        code = item.code,
        labelText = item.label,
        limit = item.limit.toString(),
        summaryText = item.summary,
        active = "${item.active}",
        distanceKmText = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        ownerId = item.ownerId,
        windowDaysText = item.windowDays.toString(),
        checksum = item.checksum,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardSnapshotViews(items: MutableList<ScorecardSnapshot>): MutableList<ScorecardSnapshotView> {
    val views = mutableListOf<ScorecardSnapshotView>()
    for (item in items) {
        views.add(toScorecardSnapshotView(item))
    }
    return views
}

fun sumScorecardSnapshotView(view: ScorecardSnapshotView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedLimit.toDouble() + parsedDistanceKm + parsedCeiling
}
