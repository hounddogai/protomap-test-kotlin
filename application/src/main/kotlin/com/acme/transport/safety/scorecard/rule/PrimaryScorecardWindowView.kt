package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary scorecard window. */
data class PrimaryScorecardWindowView(
    var code: String,
    var label: String,
    var priority: String,
    var batchSizeText: String,
    var stopCount: String,
    var budget: String,
    var category: String,
    var country: String,
    var region: String,
    var contactEmail: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toPrimaryScorecardWindowView(item: PrimaryScorecardWindow): PrimaryScorecardWindowView {
    return PrimaryScorecardWindowView(
        code = item.code,
        label = item.label,
        priority = item.priority.toString(),
        batchSizeText = item.batchSize.toString(),
        stopCount = item.stopCount.toString(),
        budget = "%.2f".format(item.budget),
        category = item.category,
        country = item.country,
        region = item.region,
        contactEmail = truncate(item.contactEmail, 64),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryScorecardWindowViews(items: MutableList<PrimaryScorecardWindow>): MutableList<PrimaryScorecardWindowView> {
    val views = mutableListOf<PrimaryScorecardWindowView>()
    for (item in items) {
        views.add(toPrimaryScorecardWindowView(item))
    }
    return views
}

fun sumPrimaryScorecardWindowView(view: PrimaryScorecardWindowView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedPriority.toDouble() + parsedBatchSize.toDouble() + parsedStopCount.toDouble()
}
