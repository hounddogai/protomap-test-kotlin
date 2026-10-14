package com.acme.transport.terminal.dockdoor.target

/** How screens and exports show a scheduled dock door draft. */
data class ScheduledDockDoorDraftView(
    var codeText: String,
    var title: String,
    var odometer: String,
    var contactEmailText: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toScheduledDockDoorDraftView(item: ScheduledDockDoorDraft): ScheduledDockDoorDraftView {
    return ScheduledDockDoorDraftView(
        codeText = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        contactEmailText = item.contactEmail,
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDockDoorDraftViews(items: MutableList<ScheduledDockDoorDraft>): MutableList<ScheduledDockDoorDraftView> {
    val views = mutableListOf<ScheduledDockDoorDraftView>()
    for (item in items) {
        views.add(toScheduledDockDoorDraftView(item))
    }
    return views
}

fun sumScheduledDockDoorDraftView(view: ScheduledDockDoorDraftView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
