package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default near miss budget. */
data class DefaultNearMissBudgetView(
    var codeText: String,
    var nameText: String,
    var vehicleId: String,
    var currency: String,
    var amount: String,
    var priorityText: String,
    var category: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toDefaultNearMissBudgetView(item: DefaultNearMissBudget): DefaultNearMissBudgetView {
    return DefaultNearMissBudgetView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        vehicleId = item.vehicleId,
        currency = item.currency,
        amount = "%.2f".format(item.amount),
        priorityText = item.priority.toString(),
        category = item.category,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNearMissBudgetViews(items: MutableList<DefaultNearMissBudget>): MutableList<DefaultNearMissBudgetView> {
    val views = mutableListOf<DefaultNearMissBudgetView>()
    for (item in items) {
        views.add(toDefaultNearMissBudgetView(item))
    }
    return views
}

fun sumDefaultNearMissBudgetView(view: DefaultNearMissBudgetView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedAmount + parsedPriority.toDouble() + parsedUpdatedAt.toDouble()
}
