package com.acme.transport.emissions.evcharge.chargeslot

/** How screens and exports show a ev charge snapshot. */
data class EvChargeSnapshotView(
    var code: String,
    var label: String,
    var fuelLitersText: String,
    var reviewer: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toEvChargeSnapshotView(item: EvChargeSnapshot): EvChargeSnapshotView {
    return EvChargeSnapshotView(
        code = item.code,
        label = item.label,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        reviewer = item.reviewer,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeSnapshotViews(items: MutableList<EvChargeSnapshot>): MutableList<EvChargeSnapshotView> {
    val views = mutableListOf<EvChargeSnapshotView>()
    for (item in items) {
        views.add(toEvChargeSnapshotView(item))
    }
    return views
}

fun sumEvChargeSnapshotView(view: EvChargeSnapshotView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedFuelLiters + parsedDurationMinutes.toDouble()
}
