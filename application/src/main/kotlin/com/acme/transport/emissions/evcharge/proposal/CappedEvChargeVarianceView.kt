package com.acme.transport.emissions.evcharge.proposal

/** How screens and exports show a capped ev charge variance. */
data class CappedEvChargeVarianceView(
    var code: String,
    var label: String,
    var thresholdText: String,
    var vehicleId: String,
    var ownerId: String,
    var pinned: String,
    var graceDays: String,
    var summaryText: String,
    var category: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toCappedEvChargeVarianceView(item: CappedEvChargeVariance): CappedEvChargeVarianceView {
    return CappedEvChargeVarianceView(
        code = item.code,
        label = item.label,
        thresholdText = "%.2f".format(item.threshold),
        vehicleId = item.vehicleId,
        ownerId = item.ownerId,
        pinned = "${item.pinned}",
        graceDays = item.graceDays.toString(),
        summaryText = item.summary,
        category = item.category,
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedEvChargeVarianceViews(items: MutableList<CappedEvChargeVariance>): MutableList<CappedEvChargeVarianceView> {
    val views = mutableListOf<CappedEvChargeVarianceView>()
    for (item in items) {
        views.add(toCappedEvChargeVarianceView(item))
    }
    return views
}

fun sumCappedEvChargeVarianceView(view: CappedEvChargeVarianceView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedThreshold + parsedGraceDays.toDouble() + parsedPallets.toDouble()
}
