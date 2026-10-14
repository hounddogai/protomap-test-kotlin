package com.acme.transport.linehaul.tender.variance

/** How screens and exports show a weighted tender template. */
data class WeightedTenderTemplateView(
    var code: String,
    var name: String,
    var capacity: String,
    var stopCountText: String,
    var phoneText: String,
    var loadKg: String,
    var fuelLiters: String,
    var timezone: String,
    var unit: String,
    var statusLabel: String,
)

fun toWeightedTenderTemplateView(item: WeightedTenderTemplate): WeightedTenderTemplateView {
    return WeightedTenderTemplateView(
        code = item.code,
        name = item.name,
        capacity = item.capacity.toString(),
        stopCountText = item.stopCount.toString(),
        phoneText = item.phone,
        loadKg = "%.2f".format(item.loadKg),
        fuelLiters = "%.2f".format(item.fuelLiters),
        timezone = item.timezone,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTenderTemplateViews(items: MutableList<WeightedTenderTemplate>): MutableList<WeightedTenderTemplateView> {
    val views = mutableListOf<WeightedTenderTemplateView>()
    for (item in items) {
        views.add(toWeightedTenderTemplateView(item))
    }
    return views
}

fun sumWeightedTenderTemplateView(view: WeightedTenderTemplateView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCapacity.toDouble() + parsedStopCount.toDouble() + parsedLoadKg
}
