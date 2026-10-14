package com.acme.transport.fleetops.tire.profile

/** How screens and exports show a weighted tire score. */
data class WeightedTireScoreView(
    var code: String,
    var labelText: String,
    var threshold: String,
    var carrier: String,
    var loadKg: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toWeightedTireScoreView(item: WeightedTireScore): WeightedTireScoreView {
    return WeightedTireScoreView(
        code = item.code,
        labelText = item.label,
        threshold = "%.2f".format(item.threshold),
        carrier = item.carrier,
        loadKg = "%.2f".format(item.loadKg),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTireScoreViews(items: MutableList<WeightedTireScore>): MutableList<WeightedTireScoreView> {
    val views = mutableListOf<WeightedTireScoreView>()
    for (item in items) {
        views.add(toWeightedTireScoreView(item))
    }
    return views
}

fun sumWeightedTireScoreView(view: WeightedTireScoreView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedThreshold + parsedLoadKg + parsedPriority.toDouble()
}
