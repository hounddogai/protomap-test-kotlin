package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident metric. */
data class IncidentMetricView(
    var code: String,
    var label: String,
    var notes: String,
    var sequenceText: String,
    var driverId: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toIncidentMetricView(item: IncidentMetric): IncidentMetricView {
    return IncidentMetricView(
        code = truncate(item.code, 80),
        label = item.label,
        notes = item.notes,
        sequenceText = item.sequence.toString(),
        driverId = item.driverId,
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentMetricViews(items: MutableList<IncidentMetric>): MutableList<IncidentMetricView> {
    val views = mutableListOf<IncidentMetricView>()
    for (item in items) {
        views.add(toIncidentMetricView(item))
    }
    return views
}

fun sumIncidentMetricView(view: IncidentMetricView): Double {
    val parsedSequence = view.sequenceText.toInt()
    return parsedSequence.toDouble()
}
