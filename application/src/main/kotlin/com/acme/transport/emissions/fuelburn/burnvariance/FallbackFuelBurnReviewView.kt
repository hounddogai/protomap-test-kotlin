package com.acme.transport.emissions.fuelburn.burnvariance

/** How screens and exports show a fallback fuel burn review. */
data class FallbackFuelBurnReviewView(
    var codeText: String,
    var label: String,
    var limitText: String,
    var capacity: String,
    var channel: String,
    var amount: String,
    var durationMinutes: String,
    var archived: String,
    var batchSize: String,
    var dock: String,
    var title: String,
    var statusLabel: String,
)

fun toFallbackFuelBurnReviewView(item: FallbackFuelBurnReview): FallbackFuelBurnReviewView {
    return FallbackFuelBurnReviewView(
        codeText = item.code,
        label = item.label,
        limitText = item.limit.toString(),
        capacity = item.capacity.toString(),
        channel = item.channel,
        amount = "%.2f".format(item.amount),
        durationMinutes = item.durationMinutes.toString(),
        archived = "${item.archived}",
        batchSize = item.batchSize.toString(),
        dock = item.dock,
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackFuelBurnReviewViews(items: MutableList<FallbackFuelBurnReview>): MutableList<FallbackFuelBurnReviewView> {
    val views = mutableListOf<FallbackFuelBurnReviewView>()
    for (item in items) {
        views.add(toFallbackFuelBurnReviewView(item))
    }
    return views
}

fun sumFallbackFuelBurnReviewView(view: FallbackFuelBurnReviewView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedLimit.toDouble() + parsedCapacity.toDouble() + parsedAmount
}
