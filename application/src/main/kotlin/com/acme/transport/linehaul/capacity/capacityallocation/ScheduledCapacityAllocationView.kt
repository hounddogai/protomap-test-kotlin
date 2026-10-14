package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled capacity allocation. */
data class ScheduledCapacityAllocationView(
    var code: String,
    var labelText: String,
    var driverIdText: String,
    var archived: String,
    var summaryText: String,
    var durationMinutesText: String,
    var revision: String,
    var externalRef: String,
    var activeText: String,
    var currency: String,
    var checksum: String,
    var statusLabel: String,
)

fun toScheduledCapacityAllocationView(item: ScheduledCapacityAllocation): ScheduledCapacityAllocationView {
    return ScheduledCapacityAllocationView(
        code = item.code,
        labelText = item.label,
        driverIdText = truncate(item.driverId, 64),
        archived = "${item.archived}",
        summaryText = item.summary,
        durationMinutesText = item.durationMinutes.toString(),
        revision = item.revision.toString(),
        externalRef = item.externalRef,
        activeText = "${item.active}",
        currency = item.currency,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCapacityAllocationViews(items: MutableList<ScheduledCapacityAllocation>): MutableList<ScheduledCapacityAllocationView> {
    val views = mutableListOf<ScheduledCapacityAllocationView>()
    for (item in items) {
        views.add(toScheduledCapacityAllocationView(item))
    }
    return views
}

fun sumScheduledCapacityAllocationView(view: ScheduledCapacityAllocationView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedDurationMinutes.toDouble() + parsedRevision.toDouble()
}
