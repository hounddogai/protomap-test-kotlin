package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict service area cycle. */
data class StrictServiceAreaCycleView(
    var code: String,
    var title: String,
    var region: String,
    var channel: String,
    var summary: String,
    var ratio: String,
    var statusLabel: String,
)

fun toStrictServiceAreaCycleView(item: StrictServiceAreaCycle): StrictServiceAreaCycleView {
    return StrictServiceAreaCycleView(
        code = truncate(item.code, 120),
        title = item.title,
        region = truncate(item.region, 64),
        channel = truncate(item.channel, 80),
        summary = item.summary,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictServiceAreaCycleViews(items: MutableList<StrictServiceAreaCycle>): MutableList<StrictServiceAreaCycleView> {
    val views = mutableListOf<StrictServiceAreaCycleView>()
    for (item in items) {
        views.add(toStrictServiceAreaCycleView(item))
    }
    return views
}

fun sumStrictServiceAreaCycleView(view: StrictServiceAreaCycleView): Double {
    val parsedRatio = view.ratio.toDouble()
    return parsedRatio
}
