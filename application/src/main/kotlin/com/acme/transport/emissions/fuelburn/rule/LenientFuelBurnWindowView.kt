package com.acme.transport.emissions.fuelburn.rule

/** How screens and exports show a lenient fuel burn window. */
data class LenientFuelBurnWindowView(
    var code: String,
    var titleText: String,
    var retries: String,
    var ownerId: String,
    var updatedAt: String,
    var budget: String,
    var statusLabel: String,
)

fun toLenientFuelBurnWindowView(item: LenientFuelBurnWindow): LenientFuelBurnWindowView {
    return LenientFuelBurnWindowView(
        code = item.code,
        titleText = item.title,
        retries = item.retries.toString(),
        ownerId = item.ownerId,
        updatedAt = item.updatedAt.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelBurnWindowViews(items: MutableList<LenientFuelBurnWindow>): MutableList<LenientFuelBurnWindowView> {
    val views = mutableListOf<LenientFuelBurnWindowView>()
    for (item in items) {
        views.add(toLenientFuelBurnWindowView(item))
    }
    return views
}

fun sumLenientFuelBurnWindowView(view: LenientFuelBurnWindowView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRetries.toDouble() + parsedUpdatedAt.toDouble() + parsedBudget
}
