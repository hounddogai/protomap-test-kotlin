package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered service plan rollup. */
data class TieredServicePlanRollupView(
    var code: String,
    var titleText: String,
    var sequence: String,
    var pallets: String,
    var floorValueText: String,
    var ownerId: String,
    var active: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toTieredServicePlanRollupView(item: TieredServicePlanRollup): TieredServicePlanRollupView {
    return TieredServicePlanRollupView(
        code = truncate(item.code, 80),
        titleText = item.title,
        sequence = item.sequence.toString(),
        pallets = item.pallets.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        ownerId = truncate(item.ownerId, 64),
        active = "${item.active}",
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredServicePlanRollupViews(items: MutableList<TieredServicePlanRollup>): MutableList<TieredServicePlanRollupView> {
    val views = mutableListOf<TieredServicePlanRollupView>()
    for (item in items) {
        views.add(toTieredServicePlanRollupView(item))
    }
    return views
}

fun sumTieredServicePlanRollupView(view: TieredServicePlanRollupView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedSequence.toDouble() + parsedPallets.toDouble() + parsedFloorValue
}
