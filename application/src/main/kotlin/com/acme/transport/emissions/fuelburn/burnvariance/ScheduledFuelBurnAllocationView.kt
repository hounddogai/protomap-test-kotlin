package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel burn allocation. */
data class ScheduledFuelBurnAllocationView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var notes: String,
    var externalRef: String,
    var weight: String,
    var statusLabel: String,
)

fun toScheduledFuelBurnAllocationView(item: ScheduledFuelBurnAllocation): ScheduledFuelBurnAllocationView {
    return ScheduledFuelBurnAllocationView(
        code = truncate(item.code, 64),
        label = item.label,
        contactEmail = item.contactEmail,
        notes = item.notes,
        externalRef = item.externalRef,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelBurnAllocationViews(items: MutableList<ScheduledFuelBurnAllocation>): MutableList<ScheduledFuelBurnAllocationView> {
    val views = mutableListOf<ScheduledFuelBurnAllocationView>()
    for (item in items) {
        views.add(toScheduledFuelBurnAllocationView(item))
    }
    return views
}

fun sumScheduledFuelBurnAllocationView(view: ScheduledFuelBurnAllocationView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
