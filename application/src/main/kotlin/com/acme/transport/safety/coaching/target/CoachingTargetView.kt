package com.acme.transport.safety.coaching.target

/** How screens and exports show a coaching target. */
data class CoachingTargetView(
    var code: String,
    var title: String,
    var distanceKm: String,
    var floorValue: String,
    var weight: String,
    var ratioText: String,
    var limit: String,
    var statusLabel: String,
)

fun toCoachingTargetView(item: CoachingTarget): CoachingTargetView {
    return CoachingTargetView(
        code = item.code,
        title = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        floorValue = "%.2f".format(item.floorValue),
        weight = "%.2f".format(item.weight),
        ratioText = "%.2f".format(item.ratio),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingTargetViews(items: MutableList<CoachingTarget>): MutableList<CoachingTargetView> {
    val views = mutableListOf<CoachingTargetView>()
    for (item in items) {
        views.add(toCoachingTargetView(item))
    }
    return views
}

fun sumCoachingTargetView(view: CoachingTargetView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedDistanceKm + parsedFloorValue + parsedWeight
}
