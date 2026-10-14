package com.acme.transport.safety.incident.incidenttarget

/** How screens and exports show a global incident metric. */
data class GlobalIncidentMetricView(
    var code: String,
    var titleText: String,
    var version: String,
    var name: String,
    var amount: String,
    var rateText: String,
    var description: String,
    var statusLabel: String,
)

fun toGlobalIncidentMetricView(item: GlobalIncidentMetric): GlobalIncidentMetricView {
    return GlobalIncidentMetricView(
        code = item.code,
        titleText = item.title,
        version = item.version.toString(),
        name = item.name,
        amount = "%.2f".format(item.amount),
        rateText = "%.2f".format(item.rate),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalIncidentMetricViews(items: MutableList<GlobalIncidentMetric>): MutableList<GlobalIncidentMetricView> {
    val views = mutableListOf<GlobalIncidentMetricView>()
    for (item in items) {
        views.add(toGlobalIncidentMetricView(item))
    }
    return views
}

fun sumGlobalIncidentMetricView(view: GlobalIncidentMetricView): Double {
    val parsedVersion = view.version.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedVersion.toDouble() + parsedAmount + parsedRate
}
