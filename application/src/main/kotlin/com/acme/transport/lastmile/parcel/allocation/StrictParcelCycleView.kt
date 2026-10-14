package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict parcel cycle. */
data class StrictParcelCycleView(
    var code: String,
    var nameText: String,
    var driverIdText: String,
    var reviewerText: String,
    var score: String,
    var batchSize: String,
    var summary: String,
    var currency: String,
    var windowDays: String,
    var capacity: String,
    var statusLabel: String,
)

fun toStrictParcelCycleView(item: StrictParcelCycle): StrictParcelCycleView {
    return StrictParcelCycleView(
        code = item.code,
        nameText = truncate(item.name, 80),
        driverIdText = item.driverId,
        reviewerText = item.reviewer,
        score = "%.2f".format(item.score),
        batchSize = item.batchSize.toString(),
        summary = item.summary,
        currency = truncate(item.currency, 40),
        windowDays = item.windowDays.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictParcelCycleViews(items: MutableList<StrictParcelCycle>): MutableList<StrictParcelCycleView> {
    val views = mutableListOf<StrictParcelCycleView>()
    for (item in items) {
        views.add(toStrictParcelCycleView(item))
    }
    return views
}

fun sumStrictParcelCycleView(view: StrictParcelCycleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedScore + parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
