package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching assignment. */
data class CoachingAssignmentView(
    var code: String,
    var title: String,
    var quantityText: String,
    var region: String,
    var description: String,
    var etaMinutesText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toCoachingAssignmentView(item: CoachingAssignment): CoachingAssignmentView {
    return CoachingAssignmentView(
        code = item.code,
        title = truncate(item.title, 80),
        quantityText = item.quantity.toString(),
        region = item.region,
        description = item.description,
        etaMinutesText = item.etaMinutes.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingAssignmentViews(items: MutableList<CoachingAssignment>): MutableList<CoachingAssignmentView> {
    val views = mutableListOf<CoachingAssignmentView>()
    for (item in items) {
        views.add(toCoachingAssignmentView(item))
    }
    return views
}

fun sumCoachingAssignmentView(view: CoachingAssignmentView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedQuantity.toDouble() + parsedEtaMinutes.toDouble() + parsedDistanceKm
}
