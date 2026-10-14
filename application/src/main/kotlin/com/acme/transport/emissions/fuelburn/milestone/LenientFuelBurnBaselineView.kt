package com.acme.transport.emissions.fuelburn.milestone

/** How screens and exports show a lenient fuel burn baseline. */
data class LenientFuelBurnBaselineView(
    var code: String,
    var name: String,
    var batchSizeText: String,
    var stopCount: String,
    var attempts: String,
    var version: String,
    var etaMinutesText: String,
    var costText: String,
    var activeText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toLenientFuelBurnBaselineView(item: LenientFuelBurnBaseline): LenientFuelBurnBaselineView {
    return LenientFuelBurnBaselineView(
        code = item.code,
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        stopCount = item.stopCount.toString(),
        attempts = item.attempts.toString(),
        version = item.version.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        costText = "%.2f".format(item.cost),
        activeText = "${item.active}",
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelBurnBaselineViews(items: MutableList<LenientFuelBurnBaseline>): MutableList<LenientFuelBurnBaselineView> {
    val views = mutableListOf<LenientFuelBurnBaselineView>()
    for (item in items) {
        views.add(toLenientFuelBurnBaselineView(item))
    }
    return views
}

fun sumLenientFuelBurnBaselineView(view: LenientFuelBurnBaselineView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedBatchSize.toDouble() + parsedStopCount.toDouble() + parsedAttempts.toDouble()
}
