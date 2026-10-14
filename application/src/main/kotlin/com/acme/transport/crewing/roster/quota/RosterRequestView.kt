package com.acme.transport.crewing.roster.quota

/** How screens and exports show a roster request. */
data class RosterRequestView(
    var code: String,
    var label: String,
    var capacity: String,
    var ceiling: String,
    var windowDaysText: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toRosterRequestView(item: RosterRequest): RosterRequestView {
    return RosterRequestView(
        code = item.code,
        label = item.label,
        capacity = item.capacity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        windowDaysText = item.windowDays.toString(),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterRequestViews(items: MutableList<RosterRequest>): MutableList<RosterRequestView> {
    val views = mutableListOf<RosterRequestView>()
    for (item in items) {
        views.add(toRosterRequestView(item))
    }
    return views
}

fun sumRosterRequestView(view: RosterRequestView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedCapacity.toDouble() + parsedCeiling + parsedWindowDays.toDouble()
}
