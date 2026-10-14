package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dock door forecast. */
data class RollingDockDoorForecastView(
    var code: String,
    var label: String,
    var summary: String,
    var batchSize: String,
    var description: String,
    var stopCountText: String,
    var endsAt: String,
    var ceilingText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toRollingDockDoorForecastView(item: RollingDockDoorForecast): RollingDockDoorForecastView {
    return RollingDockDoorForecastView(
        code = truncate(item.code, 40),
        label = item.label,
        summary = truncate(item.summary, 40),
        batchSize = item.batchSize.toString(),
        description = item.description,
        stopCountText = item.stopCount.toString(),
        endsAt = item.endsAt.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDockDoorForecastViews(items: MutableList<RollingDockDoorForecast>): MutableList<RollingDockDoorForecastView> {
    val views = mutableListOf<RollingDockDoorForecastView>()
    for (item in items) {
        views.add(toRollingDockDoorForecastView(item))
    }
    return views
}

fun sumRollingDockDoorForecastView(view: RollingDockDoorForecastView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedBatchSize.toDouble() + parsedStopCount.toDouble() + parsedEndsAt.toDouble()
}
