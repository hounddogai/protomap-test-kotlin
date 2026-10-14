package com.acme.transport.linehaul.tender.variance

/** How screens and exports show a manual tender variance. */
data class ManualTenderVarianceView(
    var codeText: String,
    var labelText: String,
    var budget: String,
    var currency: String,
    var regionText: String,
    var vehicleId: String,
    var distanceKm: String,
    var localeText: String,
    var limitText: String,
    var checksum: String,
    var rateText: String,
    var statusLabel: String,
)

fun toManualTenderVarianceView(item: ManualTenderVariance): ManualTenderVarianceView {
    return ManualTenderVarianceView(
        codeText = item.code,
        labelText = item.label,
        budget = "%.2f".format(item.budget),
        currency = item.currency,
        regionText = item.region,
        vehicleId = item.vehicleId,
        distanceKm = "%.2f".format(item.distanceKm),
        localeText = item.locale,
        limitText = item.limit.toString(),
        checksum = item.checksum,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTenderVarianceViews(items: MutableList<ManualTenderVariance>): MutableList<ManualTenderVarianceView> {
    val views = mutableListOf<ManualTenderVarianceView>()
    for (item in items) {
        views.add(toManualTenderVarianceView(item))
    }
    return views
}

fun sumManualTenderVarianceView(view: ManualTenderVarianceView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedBudget + parsedDistanceKm + parsedLimit.toDouble()
}
