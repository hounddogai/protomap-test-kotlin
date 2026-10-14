package com.acme.transport.customs.hold.holdmapping

/** How screens and exports show a manual hold profile. */
data class ManualHoldProfileView(
    var code: String,
    var labelText: String,
    var floorValue: String,
    var batchSize: String,
    var title: String,
    var cost: String,
    var distanceKm: String,
    var maximum: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toManualHoldProfileView(item: ManualHoldProfile): ManualHoldProfileView {
    return ManualHoldProfileView(
        code = item.code,
        labelText = item.label,
        floorValue = "%.2f".format(item.floorValue),
        batchSize = item.batchSize.toString(),
        title = item.title,
        cost = "%.2f".format(item.cost),
        distanceKm = "%.2f".format(item.distanceKm),
        maximum = "%.2f".format(item.maximum),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHoldProfileViews(items: MutableList<ManualHoldProfile>): MutableList<ManualHoldProfileView> {
    val views = mutableListOf<ManualHoldProfileView>()
    for (item in items) {
        views.add(toManualHoldProfileView(item))
    }
    return views
}

fun sumManualHoldProfileView(view: ManualHoldProfileView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedFloorValue + parsedBatchSize.toDouble() + parsedCost
}
