package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback coaching budget. */
data class FallbackCoachingBudgetView(
    var code: String,
    var label: String,
    var minimumText: String,
    var ceilingText: String,
    var amount: String,
    var pallets: String,
    var visible: String,
    var statusLabel: String,
)

fun toFallbackCoachingBudgetView(item: FallbackCoachingBudget): FallbackCoachingBudgetView {
    return FallbackCoachingBudgetView(
        code = truncate(item.code, 64),
        label = item.label,
        minimumText = "%.2f".format(item.minimum),
        ceilingText = "%.2f".format(item.ceiling),
        amount = "%.2f".format(item.amount),
        pallets = item.pallets.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCoachingBudgetViews(items: MutableList<FallbackCoachingBudget>): MutableList<FallbackCoachingBudgetView> {
    val views = mutableListOf<FallbackCoachingBudgetView>()
    for (item in items) {
        views.add(toFallbackCoachingBudgetView(item))
    }
    return views
}

fun sumFallbackCoachingBudgetView(view: FallbackCoachingBudgetView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedMinimum + parsedCeiling + parsedAmount
}
