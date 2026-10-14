package com.acme.transport.fleetops.tire.claim

/** How screens and exports show a tire claim. */
data class TireClaimView(
    var code: String,
    var label: String,
    var trailerIdText: String,
    var amount: String,
    var sourceText: String,
    var budget: String,
    var odometer: String,
    var stopCountText: String,
    var durationMinutesText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toTireClaimView(item: TireClaim): TireClaimView {
    return TireClaimView(
        code = item.code,
        label = item.label,
        trailerIdText = item.trailerId,
        amount = "%.2f".format(item.amount),
        sourceText = item.source,
        budget = "%.2f".format(item.budget),
        odometer = item.odometer.toString(),
        stopCountText = item.stopCount.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireClaimViews(items: MutableList<TireClaim>): MutableList<TireClaimView> {
    val views = mutableListOf<TireClaimView>()
    for (item in items) {
        views.add(toTireClaimView(item))
    }
    return views
}

fun sumTireClaimView(view: TireClaimView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedAmount + parsedBudget + parsedOdometer.toDouble()
}
