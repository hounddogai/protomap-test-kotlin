package com.acme.transport.terminal.sortation.sortationvariance

/** How screens and exports show a seasonal sortation variance. */
data class SeasonalSortationVarianceView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var visibleText: String,
    var retries: String,
    var windowDaysText: String,
    var ceilingText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toSeasonalSortationVarianceView(item: SeasonalSortationVariance): SeasonalSortationVarianceView {
    return SeasonalSortationVarianceView(
        code = item.code,
        labelText = item.label,
        ratio = "%.2f".format(item.ratio),
        visibleText = "${item.visible}",
        retries = item.retries.toString(),
        windowDaysText = item.windowDays.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalSortationVarianceViews(items: MutableList<SeasonalSortationVariance>): MutableList<SeasonalSortationVarianceView> {
    val views = mutableListOf<SeasonalSortationVarianceView>()
    for (item in items) {
        views.add(toSeasonalSortationVarianceView(item))
    }
    return views
}

fun sumSeasonalSortationVarianceView(view: SeasonalSortationVarianceView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedRatio + parsedRetries.toDouble() + parsedWindowDays.toDouble()
}
