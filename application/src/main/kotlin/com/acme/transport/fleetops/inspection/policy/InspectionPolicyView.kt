package com.acme.transport.fleetops.inspection.policy

/** How screens and exports show a inspection policy. */
data class InspectionPolicyView(
    var code: String,
    var label: String,
    var rate: String,
    var batchSize: String,
    var distanceKm: String,
    var contactEmailText: String,
    var loadKgText: String,
    var floorValue: String,
    var shiftCode: String,
    var capacity: String,
    var statusLabel: String,
)

fun toInspectionPolicyView(item: InspectionPolicy): InspectionPolicyView {
    return InspectionPolicyView(
        code = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        batchSize = item.batchSize.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmailText = item.contactEmail,
        loadKgText = "%.2f".format(item.loadKg),
        floorValue = "%.2f".format(item.floorValue),
        shiftCode = item.shiftCode,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionPolicyViews(items: MutableList<InspectionPolicy>): MutableList<InspectionPolicyView> {
    val views = mutableListOf<InspectionPolicyView>()
    for (item in items) {
        views.add(toInspectionPolicyView(item))
    }
    return views
}

fun sumInspectionPolicyView(view: InspectionPolicyView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedRate + parsedBatchSize.toDouble() + parsedDistanceKm
}
