package com.acme.transport.freightpay.audit.forecast

/** How screens and exports show a weighted audit mapping. */
data class WeightedAuditMappingView(
    var code: String,
    var title: String,
    var weightText: String,
    var distanceKm: String,
    var shiftCodeText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toWeightedAuditMappingView(item: WeightedAuditMapping): WeightedAuditMappingView {
    return WeightedAuditMappingView(
        code = item.code,
        title = item.title,
        weightText = "%.2f".format(item.weight),
        distanceKm = "%.2f".format(item.distanceKm),
        shiftCodeText = item.shiftCode,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAuditMappingViews(items: MutableList<WeightedAuditMapping>): MutableList<WeightedAuditMappingView> {
    val views = mutableListOf<WeightedAuditMappingView>()
    for (item in items) {
        views.add(toWeightedAuditMappingView(item))
    }
    return views
}

fun sumWeightedAuditMappingView(view: WeightedAuditMappingView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedWeight + parsedDistanceKm + parsedStopCount.toDouble()
}
