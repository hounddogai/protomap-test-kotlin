package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection metric. */
data class InspectionMetricView(
    var code: String,
    var name: String,
    var notes: String,
    var carrier: String,
    var distanceKm: String,
    var maximum: String,
    var trailerId: String,
    var pinned: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toInspectionMetricView(item: InspectionMetric): InspectionMetricView {
    return InspectionMetricView(
        code = truncate(item.code, 40),
        name = item.name,
        notes = item.notes,
        carrier = truncate(item.carrier, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        maximum = "%.2f".format(item.maximum),
        trailerId = truncate(item.trailerId, 80),
        pinned = "${item.pinned}",
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionMetricViews(items: MutableList<InspectionMetric>): MutableList<InspectionMetricView> {
    val views = mutableListOf<InspectionMetricView>()
    for (item in items) {
        views.add(toInspectionMetricView(item))
    }
    return views
}

fun sumInspectionMetricView(view: InspectionMetricView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedDistanceKm + parsedMaximum + parsedRevision.toDouble()
}
