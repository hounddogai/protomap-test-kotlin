package com.acme.transport.fleetops.utilization.plan

/** How screens and exports show a rolling utilization estimate. */
data class RollingUtilizationEstimateView(
    var codeText: String,
    var name: String,
    var revision: String,
    var vehicleId: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRollingUtilizationEstimateView(item: RollingUtilizationEstimate): RollingUtilizationEstimateView {
    return RollingUtilizationEstimateView(
        codeText = item.code,
        name = item.name,
        revision = item.revision.toString(),
        vehicleId = item.vehicleId,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingUtilizationEstimateViews(items: MutableList<RollingUtilizationEstimate>): MutableList<RollingUtilizationEstimateView> {
    val views = mutableListOf<RollingUtilizationEstimateView>()
    for (item in items) {
        views.add(toRollingUtilizationEstimateView(item))
    }
    return views
}

fun sumRollingUtilizationEstimateView(view: RollingUtilizationEstimateView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
