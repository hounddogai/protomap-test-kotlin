package com.acme.transport.network.transittime.allocation

/** How screens and exports show a default transit time group. */
data class DefaultTransitTimeGroupView(
    var codeText: String,
    var label: String,
    var dock: String,
    var windowDaysText: String,
    var durationMinutes: String,
    var margin: String,
    var contactEmail: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toDefaultTransitTimeGroupView(item: DefaultTransitTimeGroup): DefaultTransitTimeGroupView {
    return DefaultTransitTimeGroupView(
        codeText = item.code,
        label = item.label,
        dock = item.dock,
        windowDaysText = item.windowDays.toString(),
        durationMinutes = item.durationMinutes.toString(),
        margin = "%.2f".format(item.margin),
        contactEmail = item.contactEmail,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTransitTimeGroupViews(items: MutableList<DefaultTransitTimeGroup>): MutableList<DefaultTransitTimeGroupView> {
    val views = mutableListOf<DefaultTransitTimeGroupView>()
    for (item in items) {
        views.add(toDefaultTransitTimeGroupView(item))
    }
    return views
}

fun sumDefaultTransitTimeGroupView(view: DefaultTransitTimeGroupView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedWindowDays.toDouble() + parsedDurationMinutes.toDouble() + parsedMargin
}
