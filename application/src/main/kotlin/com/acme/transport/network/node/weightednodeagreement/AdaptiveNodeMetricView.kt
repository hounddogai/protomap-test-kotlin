package com.acme.transport.network.node.weightednodeagreement

/** How screens and exports show a adaptive node metric. */
data class AdaptiveNodeMetricView(
    var code: String,
    var title: String,
    var weightText: String,
    var revision: String,
    var rate: String,
    var archived: String,
    var version: String,
    var statusLabel: String,
)

fun toAdaptiveNodeMetricView(item: AdaptiveNodeMetric): AdaptiveNodeMetricView {
    return AdaptiveNodeMetricView(
        code = item.code,
        title = item.title,
        weightText = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        rate = "%.2f".format(item.rate),
        archived = "${item.archived}",
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNodeMetricViews(items: MutableList<AdaptiveNodeMetric>): MutableList<AdaptiveNodeMetricView> {
    val views = mutableListOf<AdaptiveNodeMetricView>()
    for (item in items) {
        views.add(toAdaptiveNodeMetricView(item))
    }
    return views
}

fun sumAdaptiveNodeMetricView(view: AdaptiveNodeMetricView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedWeight + parsedRevision.toDouble() + parsedRate
}
