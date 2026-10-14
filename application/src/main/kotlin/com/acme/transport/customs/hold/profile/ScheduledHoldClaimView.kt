package com.acme.transport.customs.hold.profile

/** How screens and exports show a scheduled hold claim. */
data class ScheduledHoldClaimView(
    var code: String,
    var name: String,
    var windowDays: String,
    var source: String,
    var expiresAt: String,
    var stopCountText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toScheduledHoldClaimView(item: ScheduledHoldClaim): ScheduledHoldClaimView {
    return ScheduledHoldClaimView(
        code = item.code,
        name = item.name,
        windowDays = item.windowDays.toString(),
        source = item.source,
        expiresAt = item.expiresAt.toString(),
        stopCountText = item.stopCount.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHoldClaimViews(items: MutableList<ScheduledHoldClaim>): MutableList<ScheduledHoldClaimView> {
    val views = mutableListOf<ScheduledHoldClaimView>()
    for (item in items) {
        views.add(toScheduledHoldClaimView(item))
    }
    return views
}

fun sumScheduledHoldClaimView(view: ScheduledHoldClaimView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedWindowDays.toDouble() + parsedExpiresAt.toDouble() + parsedStopCount.toDouble()
}
