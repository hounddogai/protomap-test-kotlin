package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

/** How screens and exports show a weighted trailer batch. */
data class WeightedTrailerBatchView(
    var code: String,
    var title: String,
    var carrier: String,
    var threshold: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toWeightedTrailerBatchView(item: WeightedTrailerBatch): WeightedTrailerBatchView {
    return WeightedTrailerBatchView(
        code = item.code,
        title = item.title,
        carrier = item.carrier,
        threshold = "%.2f".format(item.threshold),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTrailerBatchViews(items: MutableList<WeightedTrailerBatch>): MutableList<WeightedTrailerBatchView> {
    val views = mutableListOf<WeightedTrailerBatchView>()
    for (item in items) {
        views.add(toWeightedTrailerBatchView(item))
    }
    return views
}

fun sumWeightedTrailerBatchView(view: WeightedTrailerBatchView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedThreshold + parsedUpdatedAt.toDouble()
}
