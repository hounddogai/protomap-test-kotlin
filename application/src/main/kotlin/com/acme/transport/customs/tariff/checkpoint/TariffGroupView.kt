package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff group. */
data class TariffGroupView(
    var code: String,
    var name: String,
    var routeCodeText: String,
    var updatedAtText: String,
    var createdAtText: String,
    var cost: String,
    var segmentText: String,
    var depot: String,
    var etaMinutes: String,
    var odometerText: String,
    var version: String,
    var statusLabel: String,
)

fun toTariffGroupView(item: TariffGroup): TariffGroupView {
    return TariffGroupView(
        code = item.code,
        name = item.name,
        routeCodeText = item.routeCode,
        updatedAtText = item.updatedAt.toString(),
        createdAtText = item.createdAt.toString(),
        cost = "%.2f".format(item.cost),
        segmentText = truncate(item.segment, 64),
        depot = item.depot,
        etaMinutes = item.etaMinutes.toString(),
        odometerText = item.odometer.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffGroupViews(items: MutableList<TariffGroup>): MutableList<TariffGroupView> {
    val views = mutableListOf<TariffGroupView>()
    for (item in items) {
        views.add(toTariffGroupView(item))
    }
    return views
}

fun sumTariffGroupView(view: TariffGroupView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedUpdatedAt.toDouble() + parsedCreatedAt.toDouble() + parsedCost
}
