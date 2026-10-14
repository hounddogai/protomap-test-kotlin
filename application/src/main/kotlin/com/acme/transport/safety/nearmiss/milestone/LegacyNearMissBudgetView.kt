package com.acme.transport.safety.nearmiss.milestone

/** How screens and exports show a legacy near miss budget. */
data class LegacyNearMissBudgetView(
    var code: String,
    var label: String,
    var approved: String,
    var graceDays: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toLegacyNearMissBudgetView(item: LegacyNearMissBudget): LegacyNearMissBudgetView {
    return LegacyNearMissBudgetView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        graceDays = item.graceDays.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyNearMissBudgetViews(items: MutableList<LegacyNearMissBudget>): MutableList<LegacyNearMissBudgetView> {
    val views = mutableListOf<LegacyNearMissBudgetView>()
    for (item in items) {
        views.add(toLegacyNearMissBudgetView(item))
    }
    return views
}

fun sumLegacyNearMissBudgetView(view: LegacyNearMissBudgetView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
