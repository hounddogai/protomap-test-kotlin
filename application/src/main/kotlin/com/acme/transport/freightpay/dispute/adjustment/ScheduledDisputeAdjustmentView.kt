package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled dispute adjustment. */
data class ScheduledDisputeAdjustmentView(
    var codeText: String,
    var label: String,
    var driverId: String,
    var endsAtText: String,
    var updatedAt: String,
    var vehicleId: String,
    var retries: String,
    var depot: String,
    var statusLabel: String,
)

fun toScheduledDisputeAdjustmentView(item: ScheduledDisputeAdjustment): ScheduledDisputeAdjustmentView {
    return ScheduledDisputeAdjustmentView(
        codeText = item.code,
        label = item.label,
        driverId = item.driverId,
        endsAtText = item.endsAt.toString(),
        updatedAt = item.updatedAt.toString(),
        vehicleId = truncate(item.vehicleId, 40),
        retries = item.retries.toString(),
        depot = truncate(item.depot, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDisputeAdjustmentViews(items: MutableList<ScheduledDisputeAdjustment>): MutableList<ScheduledDisputeAdjustmentView> {
    val views = mutableListOf<ScheduledDisputeAdjustmentView>()
    for (item in items) {
        views.add(toScheduledDisputeAdjustmentView(item))
    }
    return views
}

fun sumScheduledDisputeAdjustmentView(view: ScheduledDisputeAdjustmentView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedEndsAt.toDouble() + parsedUpdatedAt.toDouble() + parsedRetries.toDouble()
}
