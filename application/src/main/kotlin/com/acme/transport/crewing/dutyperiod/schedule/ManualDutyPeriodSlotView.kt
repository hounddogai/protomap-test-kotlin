package com.acme.transport.crewing.dutyperiod.schedule

/** How screens and exports show a manual duty period slot. */
data class ManualDutyPeriodSlotView(
    var code: String,
    var label: String,
    var vehicleId: String,
    var revisionText: String,
    var floorValueText: String,
    var distanceKm: String,
    var unitText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toManualDutyPeriodSlotView(item: ManualDutyPeriodSlot): ManualDutyPeriodSlotView {
    return ManualDutyPeriodSlotView(
        code = item.code,
        label = item.label,
        vehicleId = item.vehicleId,
        revisionText = item.revision.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        distanceKm = "%.2f".format(item.distanceKm),
        unitText = item.unit,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodSlotViews(items: MutableList<ManualDutyPeriodSlot>): MutableList<ManualDutyPeriodSlotView> {
    val views = mutableListOf<ManualDutyPeriodSlotView>()
    for (item in items) {
        views.add(toManualDutyPeriodSlotView(item))
    }
    return views
}

fun sumManualDutyPeriodSlotView(view: ManualDutyPeriodSlotView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedRevision.toDouble() + parsedFloorValue + parsedDistanceKm
}
