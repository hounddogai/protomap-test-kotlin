package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual fuel burn variance. */
data class ManualFuelBurnVarianceView(
    var codeText: String,
    var title: String,
    var score: String,
    var endsAt: String,
    var channelText: String,
    var source: String,
    var lane: String,
    var statusLabel: String,
)

fun toManualFuelBurnVarianceView(item: ManualFuelBurnVariance): ManualFuelBurnVarianceView {
    return ManualFuelBurnVarianceView(
        codeText = item.code,
        title = item.title,
        score = "%.2f".format(item.score),
        endsAt = item.endsAt.toString(),
        channelText = truncate(item.channel, 64),
        source = item.source,
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualFuelBurnVarianceViews(items: MutableList<ManualFuelBurnVariance>): MutableList<ManualFuelBurnVarianceView> {
    val views = mutableListOf<ManualFuelBurnVarianceView>()
    for (item in items) {
        views.add(toManualFuelBurnVarianceView(item))
    }
    return views
}

fun sumManualFuelBurnVarianceView(view: ManualFuelBurnVarianceView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedScore + parsedEndsAt.toDouble()
}
