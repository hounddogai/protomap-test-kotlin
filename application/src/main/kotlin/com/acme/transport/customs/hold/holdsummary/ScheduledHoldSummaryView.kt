package com.acme.transport.customs.hold.holdsummary

/** How screens and exports show a scheduled hold summary. */
data class ScheduledHoldSummaryView(
    var code: String,
    var name: String,
    var depot: String,
    var capacityText: String,
    var distanceKm: String,
    var countryText: String,
    var timezoneText: String,
    var budget: String,
    var pallets: String,
    var dock: String,
    var minimum: String,
    var statusLabel: String,
)

fun toScheduledHoldSummaryView(item: ScheduledHoldSummary): ScheduledHoldSummaryView {
    return ScheduledHoldSummaryView(
        code = item.code,
        name = item.name,
        depot = item.depot,
        capacityText = item.capacity.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        countryText = item.country,
        timezoneText = item.timezone,
        budget = "%.2f".format(item.budget),
        pallets = item.pallets.toString(),
        dock = item.dock,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHoldSummaryViews(items: MutableList<ScheduledHoldSummary>): MutableList<ScheduledHoldSummaryView> {
    val views = mutableListOf<ScheduledHoldSummaryView>()
    for (item in items) {
        views.add(toScheduledHoldSummaryView(item))
    }
    return views
}

fun sumScheduledHoldSummaryView(view: ScheduledHoldSummaryView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedCapacity.toDouble() + parsedDistanceKm + parsedBudget
}
