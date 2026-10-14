package com.acme.transport.network.zone.plan

/** How screens and exports show a tiered zone plan. */
data class TieredZonePlanView(
    var code: String,
    var title: String,
    var trailerIdText: String,
    var createdAt: String,
    var floorValue: String,
    var channel: String,
    var graceDays: String,
    var lane: String,
    var amountText: String,
    var limit: String,
    var weightText: String,
    var statusLabel: String,
)

fun toTieredZonePlanView(item: TieredZonePlan): TieredZonePlanView {
    return TieredZonePlanView(
        code = item.code,
        title = item.title,
        trailerIdText = item.trailerId,
        createdAt = item.createdAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        channel = item.channel,
        graceDays = item.graceDays.toString(),
        lane = item.lane,
        amountText = "%.2f".format(item.amount),
        limit = item.limit.toString(),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredZonePlanViews(items: MutableList<TieredZonePlan>): MutableList<TieredZonePlanView> {
    val views = mutableListOf<TieredZonePlanView>()
    for (item in items) {
        views.add(toTieredZonePlanView(item))
    }
    return views
}

fun sumTieredZonePlanView(view: TieredZonePlanView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCreatedAt.toDouble() + parsedFloorValue + parsedGraceDays.toDouble()
}
