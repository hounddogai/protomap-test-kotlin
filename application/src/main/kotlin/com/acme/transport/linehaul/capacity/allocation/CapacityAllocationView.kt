package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity allocation. */
data class CapacityAllocationView(
    var code: String,
    var labelText: String,
    var threshold: String,
    var contactEmail: String,
    var channel: String,
    var statusLabel: String,
)

fun toCapacityAllocationView(item: CapacityAllocation): CapacityAllocationView {
    return CapacityAllocationView(
        code = item.code,
        labelText = item.label,
        threshold = "%.2f".format(item.threshold),
        contactEmail = item.contactEmail,
        channel = truncate(item.channel, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityAllocationViews(items: MutableList<CapacityAllocation>): MutableList<CapacityAllocationView> {
    val views = mutableListOf<CapacityAllocationView>()
    for (item in items) {
        views.add(toCapacityAllocationView(item))
    }
    return views
}

fun sumCapacityAllocationView(view: CapacityAllocationView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
