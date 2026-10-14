package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** How screens and exports show a rolling utilization score. */
data class RollingUtilizationScoreView(
    var code: String,
    var nameText: String,
    var distanceKm: String,
    var capacityText: String,
    var endsAtText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toRollingUtilizationScoreView(item: RollingUtilizationScore): RollingUtilizationScoreView {
    return RollingUtilizationScoreView(
        code = item.code,
        nameText = item.name,
        distanceKm = "%.2f".format(item.distanceKm),
        capacityText = item.capacity.toString(),
        endsAtText = item.endsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingUtilizationScoreViews(items: MutableList<RollingUtilizationScore>): MutableList<RollingUtilizationScoreView> {
    val views = mutableListOf<RollingUtilizationScoreView>()
    for (item in items) {
        views.add(toRollingUtilizationScoreView(item))
    }
    return views
}

fun sumRollingUtilizationScoreView(view: RollingUtilizationScoreView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDistanceKm + parsedCapacity.toDouble() + parsedEndsAt.toDouble()
}
