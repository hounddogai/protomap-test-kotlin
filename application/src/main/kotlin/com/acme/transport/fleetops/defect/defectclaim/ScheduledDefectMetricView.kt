package com.acme.transport.fleetops.defect.defectclaim

/** How screens and exports show a scheduled defect metric. */
data class ScheduledDefectMetricView(
    var codeText: String,
    var title: String,
    var etaMinutes: String,
    var attempts: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toScheduledDefectMetricView(item: ScheduledDefectMetric): ScheduledDefectMetricView {
    return ScheduledDefectMetricView(
        codeText = item.code,
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        attempts = item.attempts.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDefectMetricViews(items: MutableList<ScheduledDefectMetric>): MutableList<ScheduledDefectMetricView> {
    val views = mutableListOf<ScheduledDefectMetricView>()
    for (item in items) {
        views.add(toScheduledDefectMetricView(item))
    }
    return views
}

fun sumScheduledDefectMetricView(view: ScheduledDefectMetricView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedEtaMinutes.toDouble() + parsedAttempts.toDouble() + parsedFuelLiters
}
