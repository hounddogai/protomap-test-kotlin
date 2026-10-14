package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered hold variance. */
data class TieredHoldVarianceView(
    var code: String,
    var name: String,
    var priority: String,
    var reviewerText: String,
    var retriesText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toTieredHoldVarianceView(item: TieredHoldVariance): TieredHoldVarianceView {
    return TieredHoldVarianceView(
        code = truncate(item.code, 80),
        name = item.name,
        priority = item.priority.toString(),
        reviewerText = item.reviewer,
        retriesText = item.retries.toString(),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredHoldVarianceViews(items: MutableList<TieredHoldVariance>): MutableList<TieredHoldVarianceView> {
    val views = mutableListOf<TieredHoldVarianceView>()
    for (item in items) {
        views.add(toTieredHoldVarianceView(item))
    }
    return views
}

fun sumTieredHoldVarianceView(view: TieredHoldVarianceView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedPriority.toDouble() + parsedRetries.toDouble()
}
