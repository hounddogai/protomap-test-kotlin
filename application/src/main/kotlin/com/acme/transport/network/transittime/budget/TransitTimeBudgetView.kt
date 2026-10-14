package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time budget. */
data class TransitTimeBudgetView(
    var codeText: String,
    var titleText: String,
    var dock: String,
    var timezone: String,
    var approvedText: String,
    var graceDays: String,
    var contactEmail: String,
    var startsAt: String,
    var active: String,
    var unit: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toTransitTimeBudgetView(item: TransitTimeBudget): TransitTimeBudgetView {
    return TransitTimeBudgetView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        dock = truncate(item.dock, 64),
        timezone = item.timezone,
        approvedText = "${item.approved}",
        graceDays = item.graceDays.toString(),
        contactEmail = item.contactEmail,
        startsAt = item.startsAt.toString(),
        active = "${item.active}",
        unit = item.unit,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeBudgetViews(items: MutableList<TransitTimeBudget>): MutableList<TransitTimeBudgetView> {
    val views = mutableListOf<TransitTimeBudgetView>()
    for (item in items) {
        views.add(toTransitTimeBudgetView(item))
    }
    return views
}

fun sumTransitTimeBudgetView(view: TransitTimeBudgetView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedGraceDays.toDouble() + parsedStartsAt.toDouble() + parsedStopCount.toDouble()
}
