package com.acme.transport.freightpay.accessorial.accessorialscore

/** How screens and exports show a seasonal accessorial metric. */
data class SeasonalAccessorialMetricView(
    var codeText: String,
    var title: String,
    var unit: String,
    var weight: String,
    var stopCount: String,
    var description: String,
    var priority: String,
    var archived: String,
    var labelText: String,
    var statusLabel: String,
)

fun toSeasonalAccessorialMetricView(item: SeasonalAccessorialMetric): SeasonalAccessorialMetricView {
    return SeasonalAccessorialMetricView(
        codeText = item.code,
        title = item.title,
        unit = item.unit,
        weight = "%.2f".format(item.weight),
        stopCount = item.stopCount.toString(),
        description = item.description,
        priority = item.priority.toString(),
        archived = "${item.archived}",
        labelText = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAccessorialMetricViews(items: MutableList<SeasonalAccessorialMetric>): MutableList<SeasonalAccessorialMetricView> {
    val views = mutableListOf<SeasonalAccessorialMetricView>()
    for (item in items) {
        views.add(toSeasonalAccessorialMetricView(item))
    }
    return views
}

fun sumSeasonalAccessorialMetricView(view: SeasonalAccessorialMetricView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedWeight + parsedStopCount.toDouble() + parsedPriority.toDouble()
}
