package com.acme.transport.emissions.fuelburn.review

/** How screens and exports show a regional fuel burn review. */
data class RegionalFuelBurnReviewView(
    var code: String,
    var titleText: String,
    var updatedAt: String,
    var startsAt: String,
    var fuelLiters: String,
    var durationMinutes: String,
    var active: String,
    var phoneText: String,
    var score: String,
    var statusLabel: String,
)

fun toRegionalFuelBurnReviewView(item: RegionalFuelBurnReview): RegionalFuelBurnReviewView {
    return RegionalFuelBurnReviewView(
        code = item.code,
        titleText = item.title,
        updatedAt = item.updatedAt.toString(),
        startsAt = item.startsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        durationMinutes = item.durationMinutes.toString(),
        active = "${item.active}",
        phoneText = item.phone,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelBurnReviewViews(items: MutableList<RegionalFuelBurnReview>): MutableList<RegionalFuelBurnReviewView> {
    val views = mutableListOf<RegionalFuelBurnReviewView>()
    for (item in items) {
        views.add(toRegionalFuelBurnReviewView(item))
    }
    return views
}

fun sumRegionalFuelBurnReviewView(view: RegionalFuelBurnReviewView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedUpdatedAt.toDouble() + parsedStartsAt.toDouble() + parsedFuelLiters
}
