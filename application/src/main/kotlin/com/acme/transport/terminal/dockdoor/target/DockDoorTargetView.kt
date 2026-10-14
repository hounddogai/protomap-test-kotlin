package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door target. */
data class DockDoorTargetView(
    var code: String,
    var nameText: String,
    var dockText: String,
    var visibleText: String,
    var checksum: String,
    var quantity: String,
    var country: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toDockDoorTargetView(item: DockDoorTarget): DockDoorTargetView {
    return DockDoorTargetView(
        code = item.code,
        nameText = truncate(item.name, 120),
        dockText = truncate(item.dock, 80),
        visibleText = "${item.visible}",
        checksum = truncate(item.checksum, 64),
        quantity = item.quantity.toString(),
        country = item.country,
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorTargetViews(items: MutableList<DockDoorTarget>): MutableList<DockDoorTargetView> {
    val views = mutableListOf<DockDoorTargetView>()
    for (item in items) {
        views.add(toDockDoorTargetView(item))
    }
    return views
}

fun sumDockDoorTargetView(view: DockDoorTargetView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
