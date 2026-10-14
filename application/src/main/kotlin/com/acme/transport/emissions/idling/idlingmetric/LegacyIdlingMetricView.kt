package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy idling metric. */
data class LegacyIdlingMetricView(
    var codeText: String,
    var label: String,
    var description: String,
    var threshold: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toLegacyIdlingMetricView(item: LegacyIdlingMetric): LegacyIdlingMetricView {
    return LegacyIdlingMetricView(
        codeText = item.code,
        label = truncate(item.label, 64),
        description = item.description,
        threshold = "%.2f".format(item.threshold),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIdlingMetricViews(items: MutableList<LegacyIdlingMetric>): MutableList<LegacyIdlingMetricView> {
    val views = mutableListOf<LegacyIdlingMetricView>()
    for (item in items) {
        views.add(toLegacyIdlingMetricView(item))
    }
    return views
}

fun sumLegacyIdlingMetricView(view: LegacyIdlingMetricView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedThreshold + parsedBatchSize.toDouble()
}
