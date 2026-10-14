package com.acme.transport.terminal.crossdock.schedule

/** How screens and exports show a cross dock metric. */
data class CrossDockMetricView(
    var codeText: String,
    var label: String,
    var retries: String,
    var expiresAt: String,
    var odometer: String,
    var startsAt: String,
    var visible: String,
    var country: String,
    var cost: String,
    var ceiling: String,
    var summary: String,
    var statusLabel: String,
)

fun toCrossDockMetricView(item: CrossDockMetric): CrossDockMetricView {
    return CrossDockMetricView(
        codeText = item.code,
        label = item.label,
        retries = item.retries.toString(),
        expiresAt = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        startsAt = item.startsAt.toString(),
        visible = "${item.visible}",
        country = item.country,
        cost = "%.2f".format(item.cost),
        ceiling = "%.2f".format(item.ceiling),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockMetricViews(items: MutableList<CrossDockMetric>): MutableList<CrossDockMetricView> {
    val views = mutableListOf<CrossDockMetricView>()
    for (item in items) {
        views.add(toCrossDockMetricView(item))
    }
    return views
}

fun sumCrossDockMetricView(view: CrossDockMetricView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedRetries.toDouble() + parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
