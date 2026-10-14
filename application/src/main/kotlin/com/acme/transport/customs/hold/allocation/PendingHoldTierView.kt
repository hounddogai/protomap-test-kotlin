package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hold tier. */
data class PendingHoldTierView(
    var codeText: String,
    var label: String,
    var expiresAt: String,
    var timezone: String,
    var locale: String,
    var shiftCode: String,
    var vehicleId: String,
    var capacity: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toPendingHoldTierView(item: PendingHoldTier): PendingHoldTierView {
    return PendingHoldTierView(
        codeText = truncate(item.code, 120),
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        timezone = item.timezone,
        locale = truncate(item.locale, 64),
        shiftCode = item.shiftCode,
        vehicleId = item.vehicleId,
        capacity = item.capacity.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHoldTierViews(items: MutableList<PendingHoldTier>): MutableList<PendingHoldTierView> {
    val views = mutableListOf<PendingHoldTierView>()
    for (item in items) {
        views.add(toPendingHoldTierView(item))
    }
    return views
}

fun sumPendingHoldTierView(view: PendingHoldTierView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedCapacity.toDouble() + parsedEtaMinutes.toDouble()
}
