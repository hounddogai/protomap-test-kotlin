package com.acme.transport.crewing.dutyperiod.schedule

/** How screens and exports show a manual duty period snapshot. */
data class ManualDutyPeriodSnapshotView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var distanceKm: String,
    var capacityText: String,
    var ceilingText: String,
    var priorityText: String,
    var retries: String,
    var active: String,
    var segment: String,
    var statusLabel: String,
)

fun toManualDutyPeriodSnapshotView(item: ManualDutyPeriodSnapshot): ManualDutyPeriodSnapshotView {
    return ManualDutyPeriodSnapshotView(
        code = item.code,
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        capacityText = item.capacity.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        priorityText = item.priority.toString(),
        retries = item.retries.toString(),
        active = "${item.active}",
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodSnapshotViews(items: MutableList<ManualDutyPeriodSnapshot>): MutableList<ManualDutyPeriodSnapshotView> {
    val views = mutableListOf<ManualDutyPeriodSnapshotView>()
    for (item in items) {
        views.add(toManualDutyPeriodSnapshotView(item))
    }
    return views
}

fun sumManualDutyPeriodSnapshotView(view: ManualDutyPeriodSnapshotView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedExpiresAt.toDouble() + parsedDistanceKm + parsedCapacity.toDouble()
}
