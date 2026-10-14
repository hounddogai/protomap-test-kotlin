package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary transit time budget. */
data class PrimaryTransitTimeBudgetView(
    var code: String,
    var name: String,
    var updatedAtText: String,
    var sequence: String,
    var depot: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeBudgetView(item: PrimaryTransitTimeBudget): PrimaryTransitTimeBudgetView {
    return PrimaryTransitTimeBudgetView(
        code = truncate(item.code, 64),
        name = item.name,
        updatedAtText = item.updatedAt.toString(),
        sequence = item.sequence.toString(),
        depot = truncate(item.depot, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeBudgetViews(items: MutableList<PrimaryTransitTimeBudget>): MutableList<PrimaryTransitTimeBudgetView> {
    val views = mutableListOf<PrimaryTransitTimeBudgetView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeBudgetView(item))
    }
    return views
}

fun sumPrimaryTransitTimeBudgetView(view: PrimaryTransitTimeBudgetView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedUpdatedAt.toDouble() + parsedSequence.toDouble()
}
