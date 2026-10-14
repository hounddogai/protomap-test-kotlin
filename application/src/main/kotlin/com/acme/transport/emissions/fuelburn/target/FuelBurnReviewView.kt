package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn review. */
data class FuelBurnReviewView(
    var code: String,
    var name: String,
    var windowDays: String,
    var archived: String,
    var unit: String,
    var budget: String,
    var cost: String,
    var driverIdText: String,
    var currency: String,
    var statusLabel: String,
)

fun toFuelBurnReviewView(item: FuelBurnReview): FuelBurnReviewView {
    return FuelBurnReviewView(
        code = item.code,
        name = item.name,
        windowDays = item.windowDays.toString(),
        archived = "${item.archived}",
        unit = item.unit,
        budget = "%.2f".format(item.budget),
        cost = "%.2f".format(item.cost),
        driverIdText = truncate(item.driverId, 64),
        currency = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnReviewViews(items: MutableList<FuelBurnReview>): MutableList<FuelBurnReviewView> {
    val views = mutableListOf<FuelBurnReviewView>()
    for (item in items) {
        views.add(toFuelBurnReviewView(item))
    }
    return views
}

fun sumFuelBurnReviewView(view: FuelBurnReviewView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedWindowDays.toDouble() + parsedBudget + parsedCost
}
