package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

/** How screens and exports show a scheduled parcel mapping. */
data class ScheduledParcelMappingView(
    var code: String,
    var title: String,
    var durationMinutesText: String,
    var notes: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toScheduledParcelMappingView(item: ScheduledParcelMapping): ScheduledParcelMappingView {
    return ScheduledParcelMappingView(
        code = item.code,
        title = item.title,
        durationMinutesText = item.durationMinutes.toString(),
        notes = item.notes,
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledParcelMappingViews(items: MutableList<ScheduledParcelMapping>): MutableList<ScheduledParcelMappingView> {
    val views = mutableListOf<ScheduledParcelMappingView>()
    for (item in items) {
        views.add(toScheduledParcelMappingView(item))
    }
    return views
}

fun sumScheduledParcelMappingView(view: ScheduledParcelMappingView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDurationMinutes.toDouble()
}
