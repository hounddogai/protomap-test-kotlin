package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching window. */
data class CoachingWindowView(
    var codeText: String,
    var labelText: String,
    var discountText: String,
    var thresholdText: String,
    var vehicleIdText: String,
    var dockText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toCoachingWindowView(item: CoachingWindow): CoachingWindowView {
    return CoachingWindowView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        discountText = "%.2f".format(item.discount),
        thresholdText = "%.2f".format(item.threshold),
        vehicleIdText = truncate(item.vehicleId, 64),
        dockText = truncate(item.dock, 120),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingWindowViews(items: MutableList<CoachingWindow>): MutableList<CoachingWindowView> {
    val views = mutableListOf<CoachingWindowView>()
    for (item in items) {
        views.add(toCoachingWindowView(item))
    }
    return views
}

fun sumCoachingWindowView(view: CoachingWindowView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedDiscount + parsedThreshold + parsedStartsAt.toDouble()
}
