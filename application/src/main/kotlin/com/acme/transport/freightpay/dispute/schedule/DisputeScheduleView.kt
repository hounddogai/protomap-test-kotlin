package com.acme.transport.freightpay.dispute.schedule

/** How screens and exports show a dispute schedule. */
data class DisputeScheduleView(
    var code: String,
    var labelText: String,
    var vehicleId: String,
    var updatedAt: String,
    var retriesText: String,
    var shiftCode: String,
    var maximum: String,
    var attempts: String,
    var statusLabel: String,
)

fun toDisputeScheduleView(item: DisputeSchedule): DisputeScheduleView {
    return DisputeScheduleView(
        code = item.code,
        labelText = item.label,
        vehicleId = item.vehicleId,
        updatedAt = item.updatedAt.toString(),
        retriesText = item.retries.toString(),
        shiftCode = item.shiftCode,
        maximum = "%.2f".format(item.maximum),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeScheduleViews(items: MutableList<DisputeSchedule>): MutableList<DisputeScheduleView> {
    val views = mutableListOf<DisputeScheduleView>()
    for (item in items) {
        views.add(toDisputeScheduleView(item))
    }
    return views
}

fun sumDisputeScheduleView(view: DisputeScheduleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedUpdatedAt.toDouble() + parsedRetries.toDouble() + parsedMaximum
}
