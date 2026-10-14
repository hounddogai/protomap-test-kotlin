package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual availability budget. */
data class ManualAvailabilityBudgetView(
    var code: String,
    var label: String,
    var discount: String,
    var priorityText: String,
    var category: String,
    var minimum: String,
    var statusLabel: String,
)

fun toManualAvailabilityBudgetView(item: ManualAvailabilityBudget): ManualAvailabilityBudgetView {
    return ManualAvailabilityBudgetView(
        code = item.code,
        label = item.label,
        discount = "%.2f".format(item.discount),
        priorityText = item.priority.toString(),
        category = truncate(item.category, 80),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAvailabilityBudgetViews(items: MutableList<ManualAvailabilityBudget>): MutableList<ManualAvailabilityBudgetView> {
    val views = mutableListOf<ManualAvailabilityBudgetView>()
    for (item in items) {
        views.add(toManualAvailabilityBudgetView(item))
    }
    return views
}

fun sumManualAvailabilityBudgetView(view: ManualAvailabilityBudgetView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDiscount + parsedPriority.toDouble() + parsedMinimum
}
