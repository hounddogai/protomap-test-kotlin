package com.acme.transport.freightpay.ratecard.cardsnapshot

/** How screens and exports show a seasonal rate card snapshot. */
data class SeasonalRateCardSnapshotView(
    var code: String,
    var name: String,
    var activeText: String,
    var floorValueText: String,
    var budgetText: String,
    var odometer: String,
    var startsAt: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toSeasonalRateCardSnapshotView(item: SeasonalRateCardSnapshot): SeasonalRateCardSnapshotView {
    return SeasonalRateCardSnapshotView(
        code = item.code,
        name = item.name,
        activeText = "${item.active}",
        floorValueText = "%.2f".format(item.floorValue),
        budgetText = "%.2f".format(item.budget),
        odometer = item.odometer.toString(),
        startsAt = item.startsAt.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRateCardSnapshotViews(items: MutableList<SeasonalRateCardSnapshot>): MutableList<SeasonalRateCardSnapshotView> {
    val views = mutableListOf<SeasonalRateCardSnapshotView>()
    for (item in items) {
        views.add(toSeasonalRateCardSnapshotView(item))
    }
    return views
}

fun sumSeasonalRateCardSnapshotView(view: SeasonalRateCardSnapshotView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedFloorValue + parsedBudget + parsedOdometer.toDouble()
}
