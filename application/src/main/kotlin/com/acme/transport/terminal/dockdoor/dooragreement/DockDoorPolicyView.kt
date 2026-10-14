package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door policy. */
data class DockDoorPolicyView(
    var code: String,
    var name: String,
    var depot: String,
    var dockText: String,
    var threshold: String,
    var timezone: String,
    var statusLabel: String,
)

fun toDockDoorPolicyView(item: DockDoorPolicy): DockDoorPolicyView {
    return DockDoorPolicyView(
        code = truncate(item.code, 64),
        name = item.name,
        depot = item.depot,
        dockText = item.dock,
        threshold = "%.2f".format(item.threshold),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorPolicyViews(items: MutableList<DockDoorPolicy>): MutableList<DockDoorPolicyView> {
    val views = mutableListOf<DockDoorPolicyView>()
    for (item in items) {
        views.add(toDockDoorPolicyView(item))
    }
    return views
}

fun sumDockDoorPolicyView(view: DockDoorPolicyView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
