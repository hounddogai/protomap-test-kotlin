package com.acme.transport.crewing.roster.score

/** How screens and exports show a adaptive roster score. */
data class AdaptiveRosterScoreView(
    var code: String,
    var title: String,
    var source: String,
    var floorValue: String,
    var minimum: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toAdaptiveRosterScoreView(item: AdaptiveRosterScore): AdaptiveRosterScoreView {
    return AdaptiveRosterScoreView(
        code = item.code,
        title = item.title,
        source = item.source,
        floorValue = "%.2f".format(item.floorValue),
        minimum = "%.2f".format(item.minimum),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRosterScoreViews(items: MutableList<AdaptiveRosterScore>): MutableList<AdaptiveRosterScoreView> {
    val views = mutableListOf<AdaptiveRosterScoreView>()
    for (item in items) {
        views.add(toAdaptiveRosterScoreView(item))
    }
    return views
}

fun sumAdaptiveRosterScoreView(view: AdaptiveRosterScoreView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedFloorValue + parsedMinimum + parsedBatchSize.toDouble()
}
