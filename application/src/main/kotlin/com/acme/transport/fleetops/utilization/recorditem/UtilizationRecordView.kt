package com.acme.transport.fleetops.utilization.recorditem

/** How screens and exports show a utilization record. */
data class UtilizationRecordView(
    var code: String,
    var label: String,
    var limit: String,
    var stopCount: String,
    var fuelLiters: String,
    var discount: String,
    var vehicleId: String,
    var externalRef: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toUtilizationRecordView(item: UtilizationRecord): UtilizationRecordView {
    return UtilizationRecordView(
        code = item.code,
        label = item.label,
        limit = item.limit.toString(),
        stopCount = item.stopCount.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        discount = "%.2f".format(item.discount),
        vehicleId = item.vehicleId,
        externalRef = item.externalRef,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationRecordViews(items: MutableList<UtilizationRecord>): MutableList<UtilizationRecordView> {
    val views = mutableListOf<UtilizationRecordView>()
    for (item in items) {
        views.add(toUtilizationRecordView(item))
    }
    return views
}

fun sumUtilizationRecordView(view: UtilizationRecordView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedLimit.toDouble() + parsedStopCount.toDouble() + parsedFuelLiters
}
