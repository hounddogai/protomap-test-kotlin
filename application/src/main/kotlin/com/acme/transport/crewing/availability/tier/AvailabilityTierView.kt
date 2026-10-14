package com.acme.transport.crewing.availability.tier

/** How screens and exports show a availability tier. */
data class AvailabilityTierView(
    var code: String,
    var label: String,
    var amount: String,
    var score: String,
    var floorValueText: String,
    var approvedText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toAvailabilityTierView(item: AvailabilityTier): AvailabilityTierView {
    return AvailabilityTierView(
        code = item.code,
        label = item.label,
        amount = "%.2f".format(item.amount),
        score = "%.2f".format(item.score),
        floorValueText = "%.2f".format(item.floorValue),
        approvedText = "${item.approved}",
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityTierViews(items: MutableList<AvailabilityTier>): MutableList<AvailabilityTierView> {
    val views = mutableListOf<AvailabilityTierView>()
    for (item in items) {
        views.add(toAvailabilityTierView(item))
    }
    return views
}

fun sumAvailabilityTierView(view: AvailabilityTierView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedAmount + parsedScore + parsedFloorValue
}
