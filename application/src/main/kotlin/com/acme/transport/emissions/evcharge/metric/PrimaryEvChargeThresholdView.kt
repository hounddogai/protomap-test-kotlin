package com.acme.transport.emissions.evcharge.metric

/** How screens and exports show a primary ev charge threshold. */
data class PrimaryEvChargeThresholdView(
    var code: String,
    var name: String,
    var unitText: String,
    var priorityText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toPrimaryEvChargeThresholdView(item: PrimaryEvChargeThreshold): PrimaryEvChargeThresholdView {
    return PrimaryEvChargeThresholdView(
        code = item.code,
        name = item.name,
        unitText = item.unit,
        priorityText = item.priority.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEvChargeThresholdViews(items: MutableList<PrimaryEvChargeThreshold>): MutableList<PrimaryEvChargeThresholdView> {
    val views = mutableListOf<PrimaryEvChargeThresholdView>()
    for (item in items) {
        views.add(toPrimaryEvChargeThresholdView(item))
    }
    return views
}

fun sumPrimaryEvChargeThresholdView(view: PrimaryEvChargeThresholdView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedPriority.toDouble() + parsedScore
}
