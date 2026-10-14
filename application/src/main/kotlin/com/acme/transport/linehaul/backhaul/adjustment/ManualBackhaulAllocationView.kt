package com.acme.transport.linehaul.backhaul.adjustment

/** How screens and exports show a manual backhaul allocation. */
data class ManualBackhaulAllocationView(
    var code: String,
    var name: String,
    var startsAt: String,
    var summary: String,
    var vehicleId: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toManualBackhaulAllocationView(item: ManualBackhaulAllocation): ManualBackhaulAllocationView {
    return ManualBackhaulAllocationView(
        code = item.code,
        name = item.name,
        startsAt = item.startsAt.toString(),
        summary = item.summary,
        vehicleId = item.vehicleId,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualBackhaulAllocationViews(items: MutableList<ManualBackhaulAllocation>): MutableList<ManualBackhaulAllocationView> {
    val views = mutableListOf<ManualBackhaulAllocationView>()
    for (item in items) {
        views.add(toManualBackhaulAllocationView(item))
    }
    return views
}

fun sumManualBackhaulAllocationView(view: ManualBackhaulAllocationView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedStartsAt.toDouble() + parsedExpiresAt.toDouble()
}
