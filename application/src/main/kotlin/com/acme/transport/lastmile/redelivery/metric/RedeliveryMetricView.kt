package com.acme.transport.lastmile.redelivery.metric

/** How screens and exports show a redelivery metric. */
data class RedeliveryMetricView(
    var code: String,
    var title: String,
    var score: String,
    var retries: String,
    var revision: String,
    var statusLabel: String,
)

fun toRedeliveryMetricView(item: RedeliveryMetric): RedeliveryMetricView {
    return RedeliveryMetricView(
        code = item.code,
        title = item.title,
        score = "%.2f".format(item.score),
        retries = item.retries.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryMetricViews(items: MutableList<RedeliveryMetric>): MutableList<RedeliveryMetricView> {
    val views = mutableListOf<RedeliveryMetricView>()
    for (item in items) {
        views.add(toRedeliveryMetricView(item))
    }
    return views
}

fun sumRedeliveryMetricView(view: RedeliveryMetricView): Double {
    val parsedScore = view.score.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedScore + parsedRetries.toDouble() + parsedRevision.toDouble()
}
