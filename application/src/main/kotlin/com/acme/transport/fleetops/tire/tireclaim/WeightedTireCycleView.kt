package com.acme.transport.fleetops.tire.tireclaim

/** How screens and exports show a weighted tire cycle. */
data class WeightedTireCycleView(
    var code: String,
    var label: String,
    var graceDays: String,
    var amount: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toWeightedTireCycleView(item: WeightedTireCycle): WeightedTireCycleView {
    return WeightedTireCycleView(
        code = item.code,
        label = item.label,
        graceDays = item.graceDays.toString(),
        amount = "%.2f".format(item.amount),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTireCycleViews(items: MutableList<WeightedTireCycle>): MutableList<WeightedTireCycleView> {
    val views = mutableListOf<WeightedTireCycleView>()
    for (item in items) {
        views.add(toWeightedTireCycleView(item))
    }
    return views
}

fun sumWeightedTireCycleView(view: WeightedTireCycleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedGraceDays.toDouble() + parsedAmount + parsedCeiling
}
