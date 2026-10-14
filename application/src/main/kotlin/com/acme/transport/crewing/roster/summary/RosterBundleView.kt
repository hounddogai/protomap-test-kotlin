package com.acme.transport.crewing.roster.summary

/** How screens and exports show a roster bundle. */
data class RosterBundleView(
    var code: String,
    var name: String,
    var externalRef: String,
    var odometer: String,
    var priorityText: String,
    var unit: String,
    var statusLabel: String,
)

fun toRosterBundleView(item: RosterBundle): RosterBundleView {
    return RosterBundleView(
        code = item.code,
        name = item.name,
        externalRef = item.externalRef,
        odometer = item.odometer.toString(),
        priorityText = item.priority.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterBundleViews(items: MutableList<RosterBundle>): MutableList<RosterBundleView> {
    val views = mutableListOf<RosterBundleView>()
    for (item in items) {
        views.add(toRosterBundleView(item))
    }
    return views
}

fun sumRosterBundleView(view: RosterBundleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedOdometer.toDouble() + parsedPriority.toDouble()
}
