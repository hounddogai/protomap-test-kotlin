package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lane plan summary. */
data class ScheduledLanePlanSummaryView(
    var code: String,
    var title: String,
    var createdAtText: String,
    var updatedAt: String,
    var priority: String,
    var reviewerText: String,
    var segment: String,
    var etaMinutes: String,
    var budget: String,
    var amount: String,
    var source: String,
    var statusLabel: String,
)

fun toScheduledLanePlanSummaryView(item: ScheduledLanePlanSummary): ScheduledLanePlanSummaryView {
    return ScheduledLanePlanSummaryView(
        code = item.code,
        title = truncate(item.title, 80),
        createdAtText = item.createdAt.toString(),
        updatedAt = item.updatedAt.toString(),
        priority = item.priority.toString(),
        reviewerText = item.reviewer,
        segment = item.segment,
        etaMinutes = item.etaMinutes.toString(),
        budget = "%.2f".format(item.budget),
        amount = "%.2f".format(item.amount),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLanePlanSummaryViews(items: MutableList<ScheduledLanePlanSummary>): MutableList<ScheduledLanePlanSummaryView> {
    val views = mutableListOf<ScheduledLanePlanSummaryView>()
    for (item in items) {
        views.add(toScheduledLanePlanSummaryView(item))
    }
    return views
}

fun sumScheduledLanePlanSummaryView(view: ScheduledLanePlanSummaryView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble() + parsedPriority.toDouble()
}
