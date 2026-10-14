package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection batch. */
data class GlobalInspectionBatchView(
    var code: String,
    var label: String,
    var vehicleId: String,
    var attempts: String,
    var discount: String,
    var phone: String,
    var approvedText: String,
    var maximum: String,
    var palletsText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toGlobalInspectionBatchView(item: GlobalInspectionBatch): GlobalInspectionBatchView {
    return GlobalInspectionBatchView(
        code = truncate(item.code, 120),
        label = item.label,
        vehicleId = item.vehicleId,
        attempts = item.attempts.toString(),
        discount = "%.2f".format(item.discount),
        phone = item.phone,
        approvedText = "${item.approved}",
        maximum = "%.2f".format(item.maximum),
        palletsText = item.pallets.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionBatchViews(items: MutableList<GlobalInspectionBatch>): MutableList<GlobalInspectionBatchView> {
    val views = mutableListOf<GlobalInspectionBatchView>()
    for (item in items) {
        views.add(toGlobalInspectionBatchView(item))
    }
    return views
}

fun sumGlobalInspectionBatchView(view: GlobalInspectionBatchView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedAttempts.toDouble() + parsedDiscount + parsedMaximum
}
