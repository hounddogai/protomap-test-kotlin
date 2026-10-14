package com.acme.transport.crewing.swap.swapmapping

/** How screens and exports show a weighted swap mapping. */
data class WeightedSwapMappingView(
    var code: String,
    var label: String,
    var category: String,
    var amount: String,
    var fuelLitersText: String,
    var revision: String,
    var vehicleIdText: String,
    var durationMinutes: String,
    var weightText: String,
    var statusLabel: String,
)

fun toWeightedSwapMappingView(item: WeightedSwapMapping): WeightedSwapMappingView {
    return WeightedSwapMappingView(
        code = item.code,
        label = item.label,
        category = item.category,
        amount = "%.2f".format(item.amount),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        revision = item.revision.toString(),
        vehicleIdText = item.vehicleId,
        durationMinutes = item.durationMinutes.toString(),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedSwapMappingViews(items: MutableList<WeightedSwapMapping>): MutableList<WeightedSwapMappingView> {
    val views = mutableListOf<WeightedSwapMappingView>()
    for (item in items) {
        views.add(toWeightedSwapMappingView(item))
    }
    return views
}

fun sumWeightedSwapMappingView(view: WeightedSwapMappingView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedAmount + parsedFuelLiters + parsedRevision.toDouble()
}
