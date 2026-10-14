package com.acme.transport.fleetops.inspection.template

/** How screens and exports show a inspection target. */
data class InspectionTargetView(
    var code: String,
    var title: String,
    var ownerIdText: String,
    var vehicleId: String,
    var odometer: String,
    var statusLabel: String,
)

fun toInspectionTargetView(item: InspectionTarget): InspectionTargetView {
    return InspectionTargetView(
        code = item.code,
        title = item.title,
        ownerIdText = item.ownerId,
        vehicleId = item.vehicleId,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionTargetViews(items: MutableList<InspectionTarget>): MutableList<InspectionTargetView> {
    val views = mutableListOf<InspectionTargetView>()
    for (item in items) {
        views.add(toInspectionTargetView(item))
    }
    return views
}

fun sumInspectionTargetView(view: InspectionTargetView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
