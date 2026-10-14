package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay milestone. */
data class RelayMilestoneView(
    var code: String,
    var name: String,
    var contactEmailText: String,
    var driverId: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toRelayMilestoneView(item: RelayMilestone): RelayMilestoneView {
    return RelayMilestoneView(
        code = truncate(item.code, 64),
        name = item.name,
        contactEmailText = truncate(item.contactEmail, 64),
        driverId = item.driverId,
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayMilestoneViews(items: MutableList<RelayMilestone>): MutableList<RelayMilestoneView> {
    val views = mutableListOf<RelayMilestoneView>()
    for (item in items) {
        views.add(toRelayMilestoneView(item))
    }
    return views
}

fun sumRelayMilestoneView(view: RelayMilestoneView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
