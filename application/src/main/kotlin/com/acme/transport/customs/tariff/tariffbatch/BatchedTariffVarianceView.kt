package com.acme.transport.customs.tariff.tariffbatch

/** How screens and exports show a batched tariff variance. */
data class BatchedTariffVarianceView(
    var code: String,
    var title: String,
    var weight: String,
    var fuelLitersText: String,
    var odometerText: String,
    var source: String,
    var routeCode: String,
    var batchSizeText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toBatchedTariffVarianceView(item: BatchedTariffVariance): BatchedTariffVarianceView {
    return BatchedTariffVarianceView(
        code = item.code,
        title = item.title,
        weight = "%.2f".format(item.weight),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        odometerText = item.odometer.toString(),
        source = item.source,
        routeCode = item.routeCode,
        batchSizeText = item.batchSize.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTariffVarianceViews(items: MutableList<BatchedTariffVariance>): MutableList<BatchedTariffVarianceView> {
    val views = mutableListOf<BatchedTariffVarianceView>()
    for (item in items) {
        views.add(toBatchedTariffVarianceView(item))
    }
    return views
}

fun sumBatchedTariffVarianceView(view: BatchedTariffVarianceView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedWeight + parsedFuelLiters + parsedOdometer.toDouble()
}
