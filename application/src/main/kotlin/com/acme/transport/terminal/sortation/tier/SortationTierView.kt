package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation tier. */
data class SortationTierView(
    var code: String,
    var label: String,
    var graceDaysText: String,
    var odometer: String,
    var budget: String,
    var startsAtText: String,
    var durationMinutes: String,
    var discount: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toSortationTierView(item: SortationTier): SortationTierView {
    return SortationTierView(
        code = truncate(item.code, 40),
        label = item.label,
        graceDaysText = item.graceDays.toString(),
        odometer = item.odometer.toString(),
        budget = "%.2f".format(item.budget),
        startsAtText = item.startsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        discount = "%.2f".format(item.discount),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationTierViews(items: MutableList<SortationTier>): MutableList<SortationTierView> {
    val views = mutableListOf<SortationTierView>()
    for (item in items) {
        views.add(toSortationTierView(item))
    }
    return views
}

fun sumSortationTierView(view: SortationTierView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedGraceDays.toDouble() + parsedOdometer.toDouble() + parsedBudget
}
