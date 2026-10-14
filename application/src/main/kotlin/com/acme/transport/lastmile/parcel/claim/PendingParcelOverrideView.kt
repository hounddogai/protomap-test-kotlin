package com.acme.transport.lastmile.parcel.claim

/** How screens and exports show a pending parcel override. */
data class PendingParcelOverrideView(
    var code: String,
    var name: String,
    var distanceKmText: String,
    var visible: String,
    var driverId: String,
    var floorValueText: String,
    var attempts: String,
    var shiftCode: String,
    var ceiling: String,
    var active: String,
    var statusLabel: String,
)

fun toPendingParcelOverrideView(item: PendingParcelOverride): PendingParcelOverrideView {
    return PendingParcelOverrideView(
        code = item.code,
        name = item.name,
        distanceKmText = "%.2f".format(item.distanceKm),
        visible = "${item.visible}",
        driverId = item.driverId,
        floorValueText = "%.2f".format(item.floorValue),
        attempts = item.attempts.toString(),
        shiftCode = item.shiftCode,
        ceiling = "%.2f".format(item.ceiling),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingParcelOverrideViews(items: MutableList<PendingParcelOverride>): MutableList<PendingParcelOverrideView> {
    val views = mutableListOf<PendingParcelOverrideView>()
    for (item in items) {
        views.add(toPendingParcelOverrideView(item))
    }
    return views
}

fun sumPendingParcelOverrideView(view: PendingParcelOverrideView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedDistanceKm + parsedFloorValue + parsedAttempts.toDouble()
}
