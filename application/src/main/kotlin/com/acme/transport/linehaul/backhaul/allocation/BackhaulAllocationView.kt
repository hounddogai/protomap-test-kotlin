package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul allocation. */
data class BackhaulAllocationView(
    var code: String,
    var label: String,
    var score: String,
    var active: String,
    var budgetText: String,
    var pallets: String,
    var descriptionText: String,
    var limit: String,
    var floorValue: String,
    var durationMinutesText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toBackhaulAllocationView(item: BackhaulAllocation): BackhaulAllocationView {
    return BackhaulAllocationView(
        code = truncate(item.code, 120),
        label = item.label,
        score = "%.2f".format(item.score),
        active = "${item.active}",
        budgetText = "%.2f".format(item.budget),
        pallets = item.pallets.toString(),
        descriptionText = item.description,
        limit = item.limit.toString(),
        floorValue = "%.2f".format(item.floorValue),
        durationMinutesText = item.durationMinutes.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulAllocationViews(items: MutableList<BackhaulAllocation>): MutableList<BackhaulAllocationView> {
    val views = mutableListOf<BackhaulAllocationView>()
    for (item in items) {
        views.add(toBackhaulAllocationView(item))
    }
    return views
}

fun sumBackhaulAllocationView(view: BackhaulAllocationView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedScore + parsedBudget + parsedPallets.toDouble()
}
