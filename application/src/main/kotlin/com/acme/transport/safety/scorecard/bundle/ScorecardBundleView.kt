package com.acme.transport.safety.scorecard.bundle

/** How screens and exports show a scorecard bundle. */
data class ScorecardBundleView(
    var code: String,
    var label: String,
    var minimum: String,
    var fuelLiters: String,
    var threshold: String,
    var visibleText: String,
    var amount: String,
    var statusLabel: String,
)

fun toScorecardBundleView(item: ScorecardBundle): ScorecardBundleView {
    return ScorecardBundleView(
        code = item.code,
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        fuelLiters = "%.2f".format(item.fuelLiters),
        threshold = "%.2f".format(item.threshold),
        visibleText = "${item.visible}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardBundleViews(items: MutableList<ScorecardBundle>): MutableList<ScorecardBundleView> {
    val views = mutableListOf<ScorecardBundleView>()
    for (item in items) {
        views.add(toScorecardBundleView(item))
    }
    return views
}

fun sumScorecardBundleView(view: ScorecardBundleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMinimum + parsedFuelLiters + parsedThreshold
}
