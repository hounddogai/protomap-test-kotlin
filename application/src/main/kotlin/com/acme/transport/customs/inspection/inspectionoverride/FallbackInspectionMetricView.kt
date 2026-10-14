package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback inspection metric. */
data class FallbackInspectionMetricView(
    var codeText: String,
    var name: String,
    var vehicleId: String,
    var version: String,
    var capacityText: String,
    var batchSize: String,
    var active: String,
    var palletsText: String,
    var dock: String,
    var statusLabel: String,
)

fun toFallbackInspectionMetricView(item: FallbackInspectionMetric): FallbackInspectionMetricView {
    return FallbackInspectionMetricView(
        codeText = item.code,
        name = truncate(item.name, 64),
        vehicleId = truncate(item.vehicleId, 80),
        version = item.version.toString(),
        capacityText = item.capacity.toString(),
        batchSize = item.batchSize.toString(),
        active = "${item.active}",
        palletsText = item.pallets.toString(),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackInspectionMetricViews(items: MutableList<FallbackInspectionMetric>): MutableList<FallbackInspectionMetricView> {
    val views = mutableListOf<FallbackInspectionMetricView>()
    for (item in items) {
        views.add(toFallbackInspectionMetricView(item))
    }
    return views
}

fun sumFallbackInspectionMetricView(view: FallbackInspectionMetricView): Double {
    val parsedVersion = view.version.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedVersion.toDouble() + parsedCapacity.toDouble() + parsedBatchSize.toDouble()
}
