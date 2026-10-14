package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

/** How screens and exports show a manual proof of delivery adjustment. */
data class ManualProofOfDeliveryAdjustmentView(
    var code: String,
    var title: String,
    var timezone: String,
    var version: String,
    var source: String,
    var weightText: String,
    var floorValue: String,
    var approved: String,
    var statusLabel: String,
)

fun toManualProofOfDeliveryAdjustmentView(item: ManualProofOfDeliveryAdjustment): ManualProofOfDeliveryAdjustmentView {
    return ManualProofOfDeliveryAdjustmentView(
        code = item.code,
        title = item.title,
        timezone = item.timezone,
        version = item.version.toString(),
        source = item.source,
        weightText = "%.2f".format(item.weight),
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualProofOfDeliveryAdjustmentViews(items: MutableList<ManualProofOfDeliveryAdjustment>): MutableList<ManualProofOfDeliveryAdjustmentView> {
    val views = mutableListOf<ManualProofOfDeliveryAdjustmentView>()
    for (item in items) {
        views.add(toManualProofOfDeliveryAdjustmentView(item))
    }
    return views
}

fun sumManualProofOfDeliveryAdjustmentView(view: ManualProofOfDeliveryAdjustmentView): Double {
    val parsedVersion = view.version.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedVersion.toDouble() + parsedWeight + parsedFloorValue
}
