package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling declaration tier. */
data class RollingDeclarationTierView(
    var code: String,
    var nameText: String,
    var description: String,
    var budget: String,
    var graceDaysText: String,
    var source: String,
    var weight: String,
    var distanceKmText: String,
    var unit: String,
    var statusLabel: String,
)

fun toRollingDeclarationTierView(item: RollingDeclarationTier): RollingDeclarationTierView {
    return RollingDeclarationTierView(
        code = item.code,
        nameText = truncate(item.name, 80),
        description = item.description,
        budget = "%.2f".format(item.budget),
        graceDaysText = item.graceDays.toString(),
        source = item.source,
        weight = "%.2f".format(item.weight),
        distanceKmText = "%.2f".format(item.distanceKm),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDeclarationTierViews(items: MutableList<RollingDeclarationTier>): MutableList<RollingDeclarationTierView> {
    val views = mutableListOf<RollingDeclarationTierView>()
    for (item in items) {
        views.add(toRollingDeclarationTierView(item))
    }
    return views
}

fun sumRollingDeclarationTierView(view: RollingDeclarationTierView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedBudget + parsedGraceDays.toDouble() + parsedWeight
}
