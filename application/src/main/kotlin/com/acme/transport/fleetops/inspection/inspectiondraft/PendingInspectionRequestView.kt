package com.acme.transport.fleetops.inspection.inspectiondraft

/** How screens and exports show a pending inspection request. */
data class PendingInspectionRequestView(
    var codeText: String,
    var name: String,
    var lane: String,
    var graceDaysText: String,
    var floorValue: String,
    var stopCountText: String,
    var externalRefText: String,
    var ratioText: String,
    var pallets: String,
    var minimumText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toPendingInspectionRequestView(item: PendingInspectionRequest): PendingInspectionRequestView {
    return PendingInspectionRequestView(
        codeText = item.code,
        name = item.name,
        lane = item.lane,
        graceDaysText = item.graceDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        stopCountText = item.stopCount.toString(),
        externalRefText = item.externalRef,
        ratioText = "%.2f".format(item.ratio),
        pallets = item.pallets.toString(),
        minimumText = "%.2f".format(item.minimum),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionRequestViews(items: MutableList<PendingInspectionRequest>): MutableList<PendingInspectionRequestView> {
    val views = mutableListOf<PendingInspectionRequestView>()
    for (item in items) {
        views.add(toPendingInspectionRequestView(item))
    }
    return views
}

fun sumPendingInspectionRequestView(view: PendingInspectionRequestView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedGraceDays.toDouble() + parsedFloorValue + parsedStopCount.toDouble()
}
