package com.acme.transport.fleetops.utilization.overrideitem

/** How screens and exports show a utilization variance. */
data class UtilizationVarianceView(
    var code: String,
    var name: String,
    var limit: String,
    var windowDays: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toUtilizationVarianceView(item: UtilizationVariance): UtilizationVarianceView {
    return UtilizationVarianceView(
        code = item.code,
        name = item.name,
        limit = item.limit.toString(),
        windowDays = item.windowDays.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationVarianceViews(items: MutableList<UtilizationVariance>): MutableList<UtilizationVarianceView> {
    val views = mutableListOf<UtilizationVarianceView>()
    for (item in items) {
        views.add(toUtilizationVarianceView(item))
    }
    return views
}

fun sumUtilizationVarianceView(view: UtilizationVarianceView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedLimit.toDouble() + parsedWindowDays.toDouble() + parsedStopCount.toDouble()
}
