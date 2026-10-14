package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt profile. */
data class AttemptProfileView(
    var code: String,
    var name: String,
    var category: String,
    var odometerText: String,
    var durationMinutes: String,
    var ratio: String,
    var contactEmailText: String,
    var floorValue: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toAttemptProfileView(item: AttemptProfile): AttemptProfileView {
    return AttemptProfileView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 120),
        category = truncate(item.category, 40),
        odometerText = item.odometer.toString(),
        durationMinutes = item.durationMinutes.toString(),
        ratio = "%.2f".format(item.ratio),
        contactEmailText = item.contactEmail,
        floorValue = "%.2f".format(item.floorValue),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptProfileViews(items: MutableList<AttemptProfile>): MutableList<AttemptProfileView> {
    val views = mutableListOf<AttemptProfileView>()
    for (item in items) {
        views.add(toAttemptProfileView(item))
    }
    return views
}

fun sumAttemptProfileView(view: AttemptProfileView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedOdometer.toDouble() + parsedDurationMinutes.toDouble() + parsedRatio
}
