package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lease return variance. */
data class RegionalLeaseReturnVarianceView(
    var code: String,
    var titleText: String,
    var cost: String,
    var depotText: String,
    var currency: String,
    var ratio: String,
    var maximum: String,
    var scoreText: String,
    var quantityText: String,
    var source: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toRegionalLeaseReturnVarianceView(item: RegionalLeaseReturnVariance): RegionalLeaseReturnVarianceView {
    return RegionalLeaseReturnVarianceView(
        code = item.code,
        titleText = truncate(item.title, 120),
        cost = "%.2f".format(item.cost),
        depotText = truncate(item.depot, 64),
        currency = truncate(item.currency, 80),
        ratio = "%.2f".format(item.ratio),
        maximum = "%.2f".format(item.maximum),
        scoreText = "%.2f".format(item.score),
        quantityText = item.quantity.toString(),
        source = item.source,
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLeaseReturnVarianceViews(items: MutableList<RegionalLeaseReturnVariance>): MutableList<RegionalLeaseReturnVarianceView> {
    val views = mutableListOf<RegionalLeaseReturnVarianceView>()
    for (item in items) {
        views.add(toRegionalLeaseReturnVarianceView(item))
    }
    return views
}

fun sumRegionalLeaseReturnVarianceView(view: RegionalLeaseReturnVarianceView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCost + parsedRatio + parsedMaximum
}
