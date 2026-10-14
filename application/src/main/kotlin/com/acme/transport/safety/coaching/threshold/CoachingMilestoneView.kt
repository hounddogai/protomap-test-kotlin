package com.acme.transport.safety.coaching.threshold

/** How screens and exports show a coaching milestone. */
data class CoachingMilestoneView(
    var code: String,
    var name: String,
    var active: String,
    var carrier: String,
    var fuelLiters: String,
    var priority: String,
    var archived: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toCoachingMilestoneView(item: CoachingMilestone): CoachingMilestoneView {
    return CoachingMilestoneView(
        code = item.code,
        name = item.name,
        active = "${item.active}",
        carrier = item.carrier,
        fuelLiters = "%.2f".format(item.fuelLiters),
        priority = item.priority.toString(),
        archived = "${item.archived}",
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingMilestoneViews(items: MutableList<CoachingMilestone>): MutableList<CoachingMilestoneView> {
    val views = mutableListOf<CoachingMilestoneView>()
    for (item in items) {
        views.add(toCoachingMilestoneView(item))
    }
    return views
}

fun sumCoachingMilestoneView(view: CoachingMilestoneView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedFuelLiters + parsedPriority.toDouble() + parsedStopCount.toDouble()
}
