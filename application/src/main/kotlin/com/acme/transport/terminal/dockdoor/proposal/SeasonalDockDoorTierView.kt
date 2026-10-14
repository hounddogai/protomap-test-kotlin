package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dock door tier. */
data class SeasonalDockDoorTierView(
    var code: String,
    var name: String,
    var retries: String,
    var minimum: String,
    var currency: String,
    var priority: String,
    var budgetText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toSeasonalDockDoorTierView(item: SeasonalDockDoorTier): SeasonalDockDoorTierView {
    return SeasonalDockDoorTierView(
        code = item.code,
        name = truncate(item.name, 120),
        retries = item.retries.toString(),
        minimum = "%.2f".format(item.minimum),
        currency = item.currency,
        priority = item.priority.toString(),
        budgetText = "%.2f".format(item.budget),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDockDoorTierViews(items: MutableList<SeasonalDockDoorTier>): MutableList<SeasonalDockDoorTierView> {
    val views = mutableListOf<SeasonalDockDoorTierView>()
    for (item in items) {
        views.add(toSeasonalDockDoorTierView(item))
    }
    return views
}

fun sumSeasonalDockDoorTierView(view: SeasonalDockDoorTierView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedRetries.toDouble() + parsedMinimum + parsedPriority.toDouble()
}
