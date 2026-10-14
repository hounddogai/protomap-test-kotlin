package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock milestone. */
data class CrossDockMilestoneView(
    var code: String,
    var label: String,
    var checksum: String,
    var sequence: String,
    var vehicleId: String,
    var carrier: String,
    var dockText: String,
    var country: String,
    var ceilingText: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toCrossDockMilestoneView(item: CrossDockMilestone): CrossDockMilestoneView {
    return CrossDockMilestoneView(
        code = item.code,
        label = item.label,
        checksum = item.checksum,
        sequence = item.sequence.toString(),
        vehicleId = item.vehicleId,
        carrier = item.carrier,
        dockText = item.dock,
        country = truncate(item.country, 40),
        ceilingText = "%.2f".format(item.ceiling),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockMilestoneViews(items: MutableList<CrossDockMilestone>): MutableList<CrossDockMilestoneView> {
    val views = mutableListOf<CrossDockMilestoneView>()
    for (item in items) {
        views.add(toCrossDockMilestoneView(item))
    }
    return views
}

fun sumCrossDockMilestoneView(view: CrossDockMilestoneView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedSequence.toDouble() + parsedCeiling + parsedPallets.toDouble()
}
