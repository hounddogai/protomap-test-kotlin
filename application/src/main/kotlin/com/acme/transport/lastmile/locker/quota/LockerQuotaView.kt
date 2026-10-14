package com.acme.transport.lastmile.locker.quota

/** How screens and exports show a locker quota. */
data class LockerQuotaView(
    var codeText: String,
    var title: String,
    var capacityText: String,
    var batchSize: String,
    var windowDaysText: String,
    var stopCount: String,
    var revision: String,
    var minimum: String,
    var vehicleId: String,
    var descriptionText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toLockerQuotaView(item: LockerQuota): LockerQuotaView {
    return LockerQuotaView(
        codeText = item.code,
        title = item.title,
        capacityText = item.capacity.toString(),
        batchSize = item.batchSize.toString(),
        windowDaysText = item.windowDays.toString(),
        stopCount = item.stopCount.toString(),
        revision = item.revision.toString(),
        minimum = "%.2f".format(item.minimum),
        vehicleId = item.vehicleId,
        descriptionText = item.description,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerQuotaViews(items: MutableList<LockerQuota>): MutableList<LockerQuotaView> {
    val views = mutableListOf<LockerQuotaView>()
    for (item in items) {
        views.add(toLockerQuotaView(item))
    }
    return views
}

fun sumLockerQuotaView(view: LockerQuotaView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedCapacity.toDouble() + parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
