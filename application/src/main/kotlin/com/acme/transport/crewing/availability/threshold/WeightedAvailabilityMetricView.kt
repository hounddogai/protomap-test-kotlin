package com.acme.transport.crewing.availability.threshold

/** How screens and exports show a weighted availability metric. */
data class WeightedAvailabilityMetricView(
    var code: String,
    var labelText: String,
    var vehicleId: String,
    var durationMinutes: String,
    var checksum: String,
    var externalRef: String,
    var updatedAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toWeightedAvailabilityMetricView(item: WeightedAvailabilityMetric): WeightedAvailabilityMetricView {
    return WeightedAvailabilityMetricView(
        code = item.code,
        labelText = item.label,
        vehicleId = item.vehicleId,
        durationMinutes = item.durationMinutes.toString(),
        checksum = item.checksum,
        externalRef = item.externalRef,
        updatedAt = item.updatedAt.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAvailabilityMetricViews(items: MutableList<WeightedAvailabilityMetric>): MutableList<WeightedAvailabilityMetricView> {
    val views = mutableListOf<WeightedAvailabilityMetricView>()
    for (item in items) {
        views.add(toWeightedAvailabilityMetricView(item))
    }
    return views
}

fun sumWeightedAvailabilityMetricView(view: WeightedAvailabilityMetricView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedUpdatedAt.toDouble()
}
