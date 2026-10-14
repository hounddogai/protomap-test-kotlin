package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn variance. */
data class FuelBurnVarianceView(
    var code: String,
    var title: String,
    var sequence: String,
    var locale: String,
    var shiftCode: String,
    var timezone: String,
    var batchSize: String,
    var segmentText: String,
    var maximum: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toFuelBurnVarianceView(item: FuelBurnVariance): FuelBurnVarianceView {
    return FuelBurnVarianceView(
        code = item.code,
        title = item.title,
        sequence = item.sequence.toString(),
        locale = truncate(item.locale, 120),
        shiftCode = truncate(item.shiftCode, 40),
        timezone = item.timezone,
        batchSize = item.batchSize.toString(),
        segmentText = item.segment,
        maximum = "%.2f".format(item.maximum),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnVarianceViews(items: MutableList<FuelBurnVariance>): MutableList<FuelBurnVarianceView> {
    val views = mutableListOf<FuelBurnVarianceView>()
    for (item in items) {
        views.add(toFuelBurnVarianceView(item))
    }
    return views
}

fun sumFuelBurnVarianceView(view: FuelBurnVarianceView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedSequence.toDouble() + parsedBatchSize.toDouble() + parsedMaximum
}
