package com.acme.transport.linehaul.laneplan.checkpoint

/** How screens and exports show a lane plan checkpoint. */
data class LanePlanCheckpointView(
    var code: String,
    var labelText: String,
    var stopCountText: String,
    var segmentText: String,
    var approvedText: String,
    var marginText: String,
    var distanceKmText: String,
    var categoryText: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toLanePlanCheckpointView(item: LanePlanCheckpoint): LanePlanCheckpointView {
    return LanePlanCheckpointView(
        code = item.code,
        labelText = item.label,
        stopCountText = item.stopCount.toString(),
        segmentText = item.segment,
        approvedText = "${item.approved}",
        marginText = "%.2f".format(item.margin),
        distanceKmText = "%.2f".format(item.distanceKm),
        categoryText = item.category,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanCheckpointViews(items: MutableList<LanePlanCheckpoint>): MutableList<LanePlanCheckpointView> {
    val views = mutableListOf<LanePlanCheckpointView>()
    for (item in items) {
        views.add(toLanePlanCheckpointView(item))
    }
    return views
}

fun sumLanePlanCheckpointView(view: LanePlanCheckpointView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedStopCount.toDouble() + parsedMargin + parsedDistanceKm
}
