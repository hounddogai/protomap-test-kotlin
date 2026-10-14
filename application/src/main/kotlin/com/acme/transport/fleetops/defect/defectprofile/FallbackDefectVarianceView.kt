package com.acme.transport.fleetops.defect.defectprofile

/** How screens and exports show a fallback defect variance. */
data class FallbackDefectVarianceView(
    var code: String,
    var labelText: String,
    var reviewer: String,
    var driverIdText: String,
    var depotText: String,
    var batchSize: String,
    var capacity: String,
    var graceDaysText: String,
    var attempts: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toFallbackDefectVarianceView(item: FallbackDefectVariance): FallbackDefectVarianceView {
    return FallbackDefectVarianceView(
        code = item.code,
        labelText = item.label,
        reviewer = item.reviewer,
        driverIdText = item.driverId,
        depotText = item.depot,
        batchSize = item.batchSize.toString(),
        capacity = item.capacity.toString(),
        graceDaysText = item.graceDays.toString(),
        attempts = item.attempts.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDefectVarianceViews(items: MutableList<FallbackDefectVariance>): MutableList<FallbackDefectVarianceView> {
    val views = mutableListOf<FallbackDefectVarianceView>()
    for (item in items) {
        views.add(toFallbackDefectVarianceView(item))
    }
    return views
}

fun sumFallbackDefectVarianceView(view: FallbackDefectVarianceView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedBatchSize.toDouble() + parsedCapacity.toDouble() + parsedGraceDays.toDouble()
}
