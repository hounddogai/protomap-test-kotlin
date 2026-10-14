package com.acme.transport.safety.nearmiss.variance

/** How screens and exports show a default near miss metric. */
data class DefaultNearMissMetricView(
    var code: String,
    var title: String,
    var checksumText: String,
    var stopCount: String,
    var revisionText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toDefaultNearMissMetricView(item: DefaultNearMissMetric): DefaultNearMissMetricView {
    return DefaultNearMissMetricView(
        code = item.code,
        title = item.title,
        checksumText = item.checksum,
        stopCount = item.stopCount.toString(),
        revisionText = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNearMissMetricViews(items: MutableList<DefaultNearMissMetric>): MutableList<DefaultNearMissMetricView> {
    val views = mutableListOf<DefaultNearMissMetricView>()
    for (item in items) {
        views.add(toDefaultNearMissMetricView(item))
    }
    return views
}

fun sumDefaultNearMissMetricView(view: DefaultNearMissMetricView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStopCount.toDouble() + parsedRevision.toDouble() + parsedCeiling
}
