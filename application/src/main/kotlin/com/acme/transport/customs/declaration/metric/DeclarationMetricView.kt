package com.acme.transport.customs.declaration.metric

/** How screens and exports show a declaration metric. */
data class DeclarationMetricView(
    var code: String,
    var titleText: String,
    var threshold: String,
    var distanceKmText: String,
    var updatedAt: String,
    var ratioText: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toDeclarationMetricView(item: DeclarationMetric): DeclarationMetricView {
    return DeclarationMetricView(
        code = item.code,
        titleText = item.title,
        threshold = "%.2f".format(item.threshold),
        distanceKmText = "%.2f".format(item.distanceKm),
        updatedAt = item.updatedAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationMetricViews(items: MutableList<DeclarationMetric>): MutableList<DeclarationMetricView> {
    val views = mutableListOf<DeclarationMetricView>()
    for (item in items) {
        views.add(toDeclarationMetricView(item))
    }
    return views
}

fun sumDeclarationMetricView(view: DeclarationMetricView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedThreshold + parsedDistanceKm + parsedUpdatedAt.toDouble()
}
