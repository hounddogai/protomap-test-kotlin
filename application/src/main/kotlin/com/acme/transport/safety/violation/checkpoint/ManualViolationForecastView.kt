package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual violation forecast. */
data class ManualViolationForecastView(
    var codeText: String,
    var label: String,
    var name: String,
    var etaMinutes: String,
    var revision: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toManualViolationForecastView(item: ManualViolationForecast): ManualViolationForecastView {
    return ManualViolationForecastView(
        codeText = item.code,
        label = item.label,
        name = truncate(item.name, 120),
        etaMinutes = item.etaMinutes.toString(),
        revision = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualViolationForecastViews(items: MutableList<ManualViolationForecast>): MutableList<ManualViolationForecastView> {
    val views = mutableListOf<ManualViolationForecastView>()
    for (item in items) {
        views.add(toManualViolationForecastView(item))
    }
    return views
}

fun sumManualViolationForecastView(view: ManualViolationForecastView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRevision.toDouble() + parsedCeiling
}
