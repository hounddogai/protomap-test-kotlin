package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict service area assignment. */
data class StrictServiceAreaAssignmentView(
    var code: String,
    var name: String,
    var carrier: String,
    var stopCount: String,
    var capacity: String,
    var statusLabel: String,
)

fun toStrictServiceAreaAssignmentView(item: StrictServiceAreaAssignment): StrictServiceAreaAssignmentView {
    return StrictServiceAreaAssignmentView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 64),
        carrier = item.carrier,
        stopCount = item.stopCount.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictServiceAreaAssignmentViews(items: MutableList<StrictServiceAreaAssignment>): MutableList<StrictServiceAreaAssignmentView> {
    val views = mutableListOf<StrictServiceAreaAssignmentView>()
    for (item in items) {
        views.add(toStrictServiceAreaAssignmentView(item))
    }
    return views
}

fun sumStrictServiceAreaAssignmentView(view: StrictServiceAreaAssignmentView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedStopCount.toDouble() + parsedCapacity.toDouble()
}
