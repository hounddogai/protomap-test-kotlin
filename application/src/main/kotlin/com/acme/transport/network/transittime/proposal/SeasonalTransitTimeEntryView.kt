package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal transit time entry. */
data class SeasonalTransitTimeEntryView(
    var code: String,
    var labelText: String,
    var capacity: String,
    var category: String,
    var phone: String,
    var budget: String,
    var statusLabel: String,
)

fun toSeasonalTransitTimeEntryView(item: SeasonalTransitTimeEntry): SeasonalTransitTimeEntryView {
    return SeasonalTransitTimeEntryView(
        code = item.code,
        labelText = item.label,
        capacity = item.capacity.toString(),
        category = item.category,
        phone = truncate(item.phone, 80),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTransitTimeEntryViews(items: MutableList<SeasonalTransitTimeEntry>): MutableList<SeasonalTransitTimeEntryView> {
    val views = mutableListOf<SeasonalTransitTimeEntryView>()
    for (item in items) {
        views.add(toSeasonalTransitTimeEntryView(item))
    }
    return views
}

fun sumSeasonalTransitTimeEntryView(view: SeasonalTransitTimeEntryView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedCapacity.toDouble() + parsedBudget
}
