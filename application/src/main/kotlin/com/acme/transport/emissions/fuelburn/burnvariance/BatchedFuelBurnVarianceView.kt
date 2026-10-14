package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched fuel burn variance. */
data class BatchedFuelBurnVarianceView(
    var code: String,
    var labelText: String,
    var distanceKmText: String,
    var minimum: String,
    var startsAt: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toBatchedFuelBurnVarianceView(item: BatchedFuelBurnVariance): BatchedFuelBurnVarianceView {
    return BatchedFuelBurnVarianceView(
        code = item.code,
        labelText = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        minimum = "%.2f".format(item.minimum),
        startsAt = item.startsAt.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedFuelBurnVarianceViews(items: MutableList<BatchedFuelBurnVariance>): MutableList<BatchedFuelBurnVarianceView> {
    val views = mutableListOf<BatchedFuelBurnVarianceView>()
    for (item in items) {
        views.add(toBatchedFuelBurnVarianceView(item))
    }
    return views
}

fun sumBatchedFuelBurnVarianceView(view: BatchedFuelBurnVarianceView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedDistanceKm + parsedMinimum + parsedStartsAt.toDouble()
}
