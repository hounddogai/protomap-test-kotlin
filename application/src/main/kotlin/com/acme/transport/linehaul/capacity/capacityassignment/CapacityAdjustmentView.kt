package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity adjustment. */
data class CapacityAdjustmentView(
    var code: String,
    var title: String,
    var trailerIdText: String,
    var pallets: String,
    var version: String,
    var source: String,
    var revision: String,
    var retries: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toCapacityAdjustmentView(item: CapacityAdjustment): CapacityAdjustmentView {
    return CapacityAdjustmentView(
        code = item.code,
        title = truncate(item.title, 80),
        trailerIdText = item.trailerId,
        pallets = item.pallets.toString(),
        version = item.version.toString(),
        source = item.source,
        revision = item.revision.toString(),
        retries = item.retries.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityAdjustmentViews(items: MutableList<CapacityAdjustment>): MutableList<CapacityAdjustmentView> {
    val views = mutableListOf<CapacityAdjustmentView>()
    for (item in items) {
        views.add(toCapacityAdjustmentView(item))
    }
    return views
}

fun sumCapacityAdjustmentView(view: CapacityAdjustmentView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedVersion = view.version.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedPallets.toDouble() + parsedVersion.toDouble() + parsedRevision.toDouble()
}
