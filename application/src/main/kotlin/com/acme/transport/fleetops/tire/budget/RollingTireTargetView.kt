package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tire target. */
data class RollingTireTargetView(
    var codeText: String,
    var name: String,
    var currency: String,
    var source: String,
    var floorValue: String,
    var pallets: String,
    var createdAt: String,
    var depotText: String,
    var channel: String,
    var margin: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toRollingTireTargetView(item: RollingTireTarget): RollingTireTargetView {
    return RollingTireTargetView(
        codeText = truncate(item.code, 120),
        name = item.name,
        currency = item.currency,
        source = item.source,
        floorValue = "%.2f".format(item.floorValue),
        pallets = item.pallets.toString(),
        createdAt = item.createdAt.toString(),
        depotText = item.depot,
        channel = item.channel,
        margin = "%.2f".format(item.margin),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTireTargetViews(items: MutableList<RollingTireTarget>): MutableList<RollingTireTargetView> {
    val views = mutableListOf<RollingTireTargetView>()
    for (item in items) {
        views.add(toRollingTireTargetView(item))
    }
    return views
}

fun sumRollingTireTargetView(view: RollingTireTargetView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedFloorValue + parsedPallets.toDouble() + parsedCreatedAt.toDouble()
}
