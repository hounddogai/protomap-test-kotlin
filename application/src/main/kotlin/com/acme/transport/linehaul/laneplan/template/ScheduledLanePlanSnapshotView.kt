package com.acme.transport.linehaul.laneplan.template

/** How screens and exports show a scheduled lane plan snapshot. */
data class ScheduledLanePlanSnapshotView(
    var code: String,
    var label: String,
    var notesText: String,
    var timezone: String,
    var name: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toScheduledLanePlanSnapshotView(item: ScheduledLanePlanSnapshot): ScheduledLanePlanSnapshotView {
    return ScheduledLanePlanSnapshotView(
        code = item.code,
        label = item.label,
        notesText = item.notes,
        timezone = item.timezone,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLanePlanSnapshotViews(items: MutableList<ScheduledLanePlanSnapshot>): MutableList<ScheduledLanePlanSnapshotView> {
    val views = mutableListOf<ScheduledLanePlanSnapshotView>()
    for (item in items) {
        views.add(toScheduledLanePlanSnapshotView(item))
    }
    return views
}

fun sumScheduledLanePlanSnapshotView(view: ScheduledLanePlanSnapshotView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
