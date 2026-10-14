package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional camera event budget. */
data class RegionalCameraEventBudgetView(
    var code: String,
    var title: String,
    var capacityText: String,
    var quantityText: String,
    var channel: String,
    var activeText: String,
    var rate: String,
    var attemptsText: String,
    var retriesText: String,
    var notes: String,
    var budget: String,
    var statusLabel: String,
)

fun toRegionalCameraEventBudgetView(item: RegionalCameraEventBudget): RegionalCameraEventBudgetView {
    return RegionalCameraEventBudgetView(
        code = truncate(item.code, 64),
        title = item.title,
        capacityText = item.capacity.toString(),
        quantityText = item.quantity.toString(),
        channel = truncate(item.channel, 80),
        activeText = "${item.active}",
        rate = "%.2f".format(item.rate),
        attemptsText = item.attempts.toString(),
        retriesText = item.retries.toString(),
        notes = truncate(item.notes, 40),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCameraEventBudgetViews(items: MutableList<RegionalCameraEventBudget>): MutableList<RegionalCameraEventBudgetView> {
    val views = mutableListOf<RegionalCameraEventBudgetView>()
    for (item in items) {
        views.add(toRegionalCameraEventBudgetView(item))
    }
    return views
}

fun sumRegionalCameraEventBudgetView(view: RegionalCameraEventBudgetView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCapacity.toDouble() + parsedQuantity.toDouble() + parsedRate
}
