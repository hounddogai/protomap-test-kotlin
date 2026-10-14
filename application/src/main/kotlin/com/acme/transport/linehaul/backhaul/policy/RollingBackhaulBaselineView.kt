package com.acme.transport.linehaul.backhaul.policy

/** How screens and exports show a rolling backhaul baseline. */
data class RollingBackhaulBaselineView(
    var codeText: String,
    var titleText: String,
    var floorValue: String,
    var phone: String,
    var fuelLitersText: String,
    var updatedAt: String,
    var locale: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toRollingBackhaulBaselineView(item: RollingBackhaulBaseline): RollingBackhaulBaselineView {
    return RollingBackhaulBaselineView(
        codeText = item.code,
        titleText = item.title,
        floorValue = "%.2f".format(item.floorValue),
        phone = item.phone,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        updatedAt = item.updatedAt.toString(),
        locale = item.locale,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingBackhaulBaselineViews(items: MutableList<RollingBackhaulBaseline>): MutableList<RollingBackhaulBaselineView> {
    val views = mutableListOf<RollingBackhaulBaselineView>()
    for (item in items) {
        views.add(toRollingBackhaulBaselineView(item))
    }
    return views
}

fun sumRollingBackhaulBaselineView(view: RollingBackhaulBaselineView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedFloorValue + parsedFuelLiters + parsedUpdatedAt.toDouble()
}
