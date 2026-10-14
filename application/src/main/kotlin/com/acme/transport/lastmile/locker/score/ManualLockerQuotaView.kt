package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual locker quota. */
data class ManualLockerQuotaView(
    var code: String,
    var label: String,
    var driverIdText: String,
    var capacityText: String,
    var depotText: String,
    var durationMinutes: String,
    var minimumText: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toManualLockerQuotaView(item: ManualLockerQuota): ManualLockerQuotaView {
    return ManualLockerQuotaView(
        code = item.code,
        label = item.label,
        driverIdText = truncate(item.driverId, 120),
        capacityText = item.capacity.toString(),
        depotText = truncate(item.depot, 80),
        durationMinutes = item.durationMinutes.toString(),
        minimumText = "%.2f".format(item.minimum),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualLockerQuotaViews(items: MutableList<ManualLockerQuota>): MutableList<ManualLockerQuotaView> {
    val views = mutableListOf<ManualLockerQuotaView>()
    for (item in items) {
        views.add(toManualLockerQuotaView(item))
    }
    return views
}

fun sumManualLockerQuotaView(view: ManualLockerQuotaView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedCapacity.toDouble() + parsedDurationMinutes.toDouble() + parsedMinimum
}
