package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted swap segment. */
data class WeightedSwapSegmentView(
    var codeText: String,
    var name: String,
    var createdAt: String,
    var unitText: String,
    var capacity: String,
    var notesText: String,
    var budgetText: String,
    var ownerId: String,
    var revision: String,
    var amount: String,
    var statusLabel: String,
)

fun toWeightedSwapSegmentView(item: WeightedSwapSegment): WeightedSwapSegmentView {
    return WeightedSwapSegmentView(
        codeText = item.code,
        name = item.name,
        createdAt = item.createdAt.toString(),
        unitText = item.unit,
        capacity = item.capacity.toString(),
        notesText = truncate(item.notes, 40),
        budgetText = "%.2f".format(item.budget),
        ownerId = truncate(item.ownerId, 80),
        revision = item.revision.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedSwapSegmentViews(items: MutableList<WeightedSwapSegment>): MutableList<WeightedSwapSegmentView> {
    val views = mutableListOf<WeightedSwapSegmentView>()
    for (item in items) {
        views.add(toWeightedSwapSegmentView(item))
    }
    return views
}

fun sumWeightedSwapSegmentView(view: WeightedSwapSegmentView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedCreatedAt.toDouble() + parsedCapacity.toDouble() + parsedBudget
}
