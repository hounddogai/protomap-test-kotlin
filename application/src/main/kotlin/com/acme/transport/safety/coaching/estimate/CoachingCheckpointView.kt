package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching checkpoint. */
data class CoachingCheckpointView(
    var codeText: String,
    var name: String,
    var minimumText: String,
    var quantityText: String,
    var depot: String,
    var statusLabel: String,
)

fun toCoachingCheckpointView(item: CoachingCheckpoint): CoachingCheckpointView {
    return CoachingCheckpointView(
        codeText = item.code,
        name = truncate(item.name, 120),
        minimumText = "%.2f".format(item.minimum),
        quantityText = item.quantity.toString(),
        depot = truncate(item.depot, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingCheckpointViews(items: MutableList<CoachingCheckpoint>): MutableList<CoachingCheckpointView> {
    val views = mutableListOf<CoachingCheckpointView>()
    for (item in items) {
        views.add(toCoachingCheckpointView(item))
    }
    return views
}

fun sumCoachingCheckpointView(view: CoachingCheckpointView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedMinimum + parsedQuantity.toDouble()
}
