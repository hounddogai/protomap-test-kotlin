package com.acme.transport.emissions.fuelburn.recorditem

/** How screens and exports show a fuel burn score. */
data class FuelBurnScoreView(
    var code: String,
    var labelText: String,
    var etaMinutesText: String,
    var rate: String,
    var createdAtText: String,
    var budgetText: String,
    var windowDays: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toFuelBurnScoreView(item: FuelBurnScore): FuelBurnScoreView {
    return FuelBurnScoreView(
        code = item.code,
        labelText = item.label,
        etaMinutesText = item.etaMinutes.toString(),
        rate = "%.2f".format(item.rate),
        createdAtText = item.createdAt.toString(),
        budgetText = "%.2f".format(item.budget),
        windowDays = item.windowDays.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnScoreViews(items: MutableList<FuelBurnScore>): MutableList<FuelBurnScoreView> {
    val views = mutableListOf<FuelBurnScoreView>()
    for (item in items) {
        views.add(toFuelBurnScoreView(item))
    }
    return views
}

fun sumFuelBurnScoreView(view: FuelBurnScoreView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedEtaMinutes.toDouble() + parsedRate + parsedCreatedAt.toDouble()
}
