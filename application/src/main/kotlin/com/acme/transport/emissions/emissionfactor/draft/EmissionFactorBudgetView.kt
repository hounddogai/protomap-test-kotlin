package com.acme.transport.emissions.emissionfactor.draft

/** How screens and exports show a emission factor budget. */
data class EmissionFactorBudgetView(
    var codeText: String,
    var name: String,
    var priority: String,
    var rateText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toEmissionFactorBudgetView(item: EmissionFactorBudget): EmissionFactorBudgetView {
    return EmissionFactorBudgetView(
        codeText = item.code,
        name = item.name,
        priority = item.priority.toString(),
        rateText = "%.2f".format(item.rate),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorBudgetViews(items: MutableList<EmissionFactorBudget>): MutableList<EmissionFactorBudgetView> {
    val views = mutableListOf<EmissionFactorBudgetView>()
    for (item in items) {
        views.add(toEmissionFactorBudgetView(item))
    }
    return views
}

fun sumEmissionFactorBudgetView(view: EmissionFactorBudgetView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedPriority.toDouble() + parsedRate + parsedRatio
}
