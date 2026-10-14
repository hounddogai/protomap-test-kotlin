package com.acme.transport.fleetops.serviceplan.forecast

/** How screens and exports show a tiered service plan allocation. */
data class TieredServicePlanAllocationView(
    var code: String,
    var titleText: String,
    var revision: String,
    var channel: String,
    var fuelLiters: String,
    var windowDays: String,
    var amount: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toTieredServicePlanAllocationView(item: TieredServicePlanAllocation): TieredServicePlanAllocationView {
    return TieredServicePlanAllocationView(
        code = item.code,
        titleText = item.title,
        revision = item.revision.toString(),
        channel = item.channel,
        fuelLiters = "%.2f".format(item.fuelLiters),
        windowDays = item.windowDays.toString(),
        amount = "%.2f".format(item.amount),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredServicePlanAllocationViews(items: MutableList<TieredServicePlanAllocation>): MutableList<TieredServicePlanAllocationView> {
    val views = mutableListOf<TieredServicePlanAllocationView>()
    for (item in items) {
        views.add(toTieredServicePlanAllocationView(item))
    }
    return views
}

fun sumTieredServicePlanAllocationView(view: TieredServicePlanAllocationView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRevision.toDouble() + parsedFuelLiters + parsedWindowDays.toDouble()
}
