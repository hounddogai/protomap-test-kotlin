package com.acme.transport.safety.incident.bundle

/** How screens and exports show a incident bundle. */
data class IncidentBundleView(
    var code: String,
    var nameText: String,
    var carrierText: String,
    var distanceKm: String,
    var retries: String,
    var priority: String,
    var thresholdText: String,
    var locale: String,
    var amount: String,
    var statusLabel: String,
)

fun toIncidentBundleView(item: IncidentBundle): IncidentBundleView {
    return IncidentBundleView(
        code = item.code,
        nameText = item.name,
        carrierText = item.carrier,
        distanceKm = "%.2f".format(item.distanceKm),
        retries = item.retries.toString(),
        priority = item.priority.toString(),
        thresholdText = "%.2f".format(item.threshold),
        locale = item.locale,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentBundleViews(items: MutableList<IncidentBundle>): MutableList<IncidentBundleView> {
    val views = mutableListOf<IncidentBundleView>()
    for (item in items) {
        views.add(toIncidentBundleView(item))
    }
    return views
}

fun sumIncidentBundleView(view: IncidentBundleView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDistanceKm + parsedRetries.toDouble() + parsedPriority.toDouble()
}
