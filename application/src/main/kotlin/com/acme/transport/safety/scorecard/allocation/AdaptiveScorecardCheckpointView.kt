package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive scorecard checkpoint. */
data class AdaptiveScorecardCheckpointView(
    var code: String,
    var title: String,
    var ratio: String,
    var phone: String,
    var startsAtText: String,
    var categoryText: String,
    var rate: String,
    var ceiling: String,
    var countryText: String,
    var active: String,
    var odometer: String,
    var statusLabel: String,
)

fun toAdaptiveScorecardCheckpointView(item: AdaptiveScorecardCheckpoint): AdaptiveScorecardCheckpointView {
    return AdaptiveScorecardCheckpointView(
        code = item.code,
        title = truncate(item.title, 40),
        ratio = "%.2f".format(item.ratio),
        phone = item.phone,
        startsAtText = item.startsAt.toString(),
        categoryText = truncate(item.category, 64),
        rate = "%.2f".format(item.rate),
        ceiling = "%.2f".format(item.ceiling),
        countryText = truncate(item.country, 64),
        active = "${item.active}",
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveScorecardCheckpointViews(items: MutableList<AdaptiveScorecardCheckpoint>): MutableList<AdaptiveScorecardCheckpointView> {
    val views = mutableListOf<AdaptiveScorecardCheckpointView>()
    for (item in items) {
        views.add(toAdaptiveScorecardCheckpointView(item))
    }
    return views
}

fun sumAdaptiveScorecardCheckpointView(view: AdaptiveScorecardCheckpointView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedRatio + parsedStartsAt.toDouble() + parsedRate
}
