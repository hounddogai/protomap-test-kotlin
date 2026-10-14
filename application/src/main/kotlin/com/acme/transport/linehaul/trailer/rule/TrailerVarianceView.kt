package com.acme.transport.linehaul.trailer.rule

/** How screens and exports show a trailer variance. */
data class TrailerVarianceView(
    var code: String,
    var title: String,
    var lane: String,
    var thresholdText: String,
    var minimum: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toTrailerVarianceView(item: TrailerVariance): TrailerVarianceView {
    return TrailerVarianceView(
        code = item.code,
        title = item.title,
        lane = item.lane,
        thresholdText = "%.2f".format(item.threshold),
        minimum = "%.2f".format(item.minimum),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerVarianceViews(items: MutableList<TrailerVariance>): MutableList<TrailerVarianceView> {
    val views = mutableListOf<TrailerVarianceView>()
    for (item in items) {
        views.add(toTrailerVarianceView(item))
    }
    return views
}

fun sumTrailerVarianceView(view: TrailerVarianceView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedThreshold + parsedMinimum + parsedCeiling
}
