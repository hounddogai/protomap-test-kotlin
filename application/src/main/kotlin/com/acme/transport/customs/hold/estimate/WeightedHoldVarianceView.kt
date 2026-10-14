package com.acme.transport.customs.hold.estimate

/** How screens and exports show a weighted hold variance. */
data class WeightedHoldVarianceView(
    var code: String,
    var labelText: String,
    var etaMinutesText: String,
    var graceDays: String,
    var amount: String,
    var score: String,
    var statusLabel: String,
)

fun toWeightedHoldVarianceView(item: WeightedHoldVariance): WeightedHoldVarianceView {
    return WeightedHoldVarianceView(
        code = item.code,
        labelText = item.label,
        etaMinutesText = item.etaMinutes.toString(),
        graceDays = item.graceDays.toString(),
        amount = "%.2f".format(item.amount),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedHoldVarianceViews(items: MutableList<WeightedHoldVariance>): MutableList<WeightedHoldVarianceView> {
    val views = mutableListOf<WeightedHoldVarianceView>()
    for (item in items) {
        views.add(toWeightedHoldVarianceView(item))
    }
    return views
}

fun sumWeightedHoldVarianceView(view: WeightedHoldVarianceView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedEtaMinutes.toDouble() + parsedGraceDays.toDouble() + parsedAmount
}
