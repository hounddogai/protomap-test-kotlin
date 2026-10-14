package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled transit time summary. */
data class ScheduledTransitTimeSummaryView(
    var code: String,
    var name: String,
    var budgetText: String,
    var updatedAt: String,
    var depotText: String,
    var sourceText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toScheduledTransitTimeSummaryView(item: ScheduledTransitTimeSummary): ScheduledTransitTimeSummaryView {
    return ScheduledTransitTimeSummaryView(
        code = item.code,
        name = truncate(item.name, 40),
        budgetText = "%.2f".format(item.budget),
        updatedAt = item.updatedAt.toString(),
        depotText = truncate(item.depot, 80),
        sourceText = truncate(item.source, 40),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTransitTimeSummaryViews(items: MutableList<ScheduledTransitTimeSummary>): MutableList<ScheduledTransitTimeSummaryView> {
    val views = mutableListOf<ScheduledTransitTimeSummaryView>()
    for (item in items) {
        views.add(toScheduledTransitTimeSummaryView(item))
    }
    return views
}

fun sumScheduledTransitTimeSummaryView(view: ScheduledTransitTimeSummaryView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedBudget + parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble()
}
