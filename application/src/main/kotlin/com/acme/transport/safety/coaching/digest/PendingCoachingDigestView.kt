package com.acme.transport.safety.coaching.digest

/** How screens and exports show a pending coaching digest. */
data class PendingCoachingDigestView(
    var code: String,
    var name: String,
    var odometer: String,
    var vehicleId: String,
    var capacity: String,
    var minimum: String,
    var regionText: String,
    var maximum: String,
    var trailerId: String,
    var archived: String,
    var statusLabel: String,
)

fun toPendingCoachingDigestView(item: PendingCoachingDigest): PendingCoachingDigestView {
    return PendingCoachingDigestView(
        code = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        vehicleId = item.vehicleId,
        capacity = item.capacity.toString(),
        minimum = "%.2f".format(item.minimum),
        regionText = item.region,
        maximum = "%.2f".format(item.maximum),
        trailerId = item.trailerId,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCoachingDigestViews(items: MutableList<PendingCoachingDigest>): MutableList<PendingCoachingDigestView> {
    val views = mutableListOf<PendingCoachingDigestView>()
    for (item in items) {
        views.add(toPendingCoachingDigestView(item))
    }
    return views
}

fun sumPendingCoachingDigestView(view: PendingCoachingDigestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedOdometer.toDouble() + parsedCapacity.toDouble() + parsedMinimum
}
