package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending violation snapshot. */
data class PendingViolationSnapshotView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var labelText: String,
    var archived: String,
    var channel: String,
    var fuelLiters: String,
    var odometer: String,
    var endsAt: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toPendingViolationSnapshotView(item: PendingViolationSnapshot): PendingViolationSnapshotView {
    return PendingViolationSnapshotView(
        code = item.code,
        name = truncate(item.name, 64),
        etaMinutes = item.etaMinutes.toString(),
        labelText = item.label,
        archived = "${item.archived}",
        channel = item.channel,
        fuelLiters = "%.2f".format(item.fuelLiters),
        odometer = item.odometer.toString(),
        endsAt = item.endsAt.toString(),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingViolationSnapshotViews(items: MutableList<PendingViolationSnapshot>): MutableList<PendingViolationSnapshotView> {
    val views = mutableListOf<PendingViolationSnapshotView>()
    for (item in items) {
        views.add(toPendingViolationSnapshotView(item))
    }
    return views
}

fun sumPendingViolationSnapshotView(view: PendingViolationSnapshotView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedEtaMinutes.toDouble() + parsedFuelLiters + parsedOdometer.toDouble()
}
