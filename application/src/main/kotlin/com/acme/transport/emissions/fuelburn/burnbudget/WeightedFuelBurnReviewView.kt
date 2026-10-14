package com.acme.transport.emissions.fuelburn.burnbudget

/** How screens and exports show a weighted fuel burn review. */
data class WeightedFuelBurnReviewView(
    var code: String,
    var name: String,
    var revision: String,
    var limit: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnReviewView(item: WeightedFuelBurnReview): WeightedFuelBurnReviewView {
    return WeightedFuelBurnReviewView(
        code = item.code,
        name = item.name,
        revision = item.revision.toString(),
        limit = item.limit.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnReviewViews(items: MutableList<WeightedFuelBurnReview>): MutableList<WeightedFuelBurnReviewView> {
    val views = mutableListOf<WeightedFuelBurnReviewView>()
    for (item in items) {
        views.add(toWeightedFuelBurnReviewView(item))
    }
    return views
}

fun sumWeightedFuelBurnReviewView(view: WeightedFuelBurnReviewView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedRevision.toDouble() + parsedLimit.toDouble()
}
