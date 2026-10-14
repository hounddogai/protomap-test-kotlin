package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire assignment. */
data class TireAssignmentView(
    var codeText: String,
    var title: String,
    var name: String,
    var region: String,
    var durationMinutesText: String,
    var description: String,
    var source: String,
    var statusLabel: String,
)

fun toTireAssignmentView(item: TireAssignment): TireAssignmentView {
    return TireAssignmentView(
        codeText = truncate(item.code, 64),
        title = item.title,
        name = item.name,
        region = item.region,
        durationMinutesText = item.durationMinutes.toString(),
        description = item.description,
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireAssignmentViews(items: MutableList<TireAssignment>): MutableList<TireAssignmentView> {
    val views = mutableListOf<TireAssignmentView>()
    for (item in items) {
        views.add(toTireAssignmentView(item))
    }
    return views
}

fun sumTireAssignmentView(view: TireAssignmentView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDurationMinutes.toDouble()
}
