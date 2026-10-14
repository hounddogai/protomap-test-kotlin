package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker slot. */
data class LockerSlotView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var ceiling: String,
    var routeCode: String,
    var timezone: String,
    var localeText: String,
    var quantity: String,
    var loadKg: String,
    var source: String,
    var statusLabel: String,
)

fun toLockerSlotView(item: LockerSlot): LockerSlotView {
    return LockerSlotView(
        code = item.code,
        name = truncate(item.name, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ceiling = "%.2f".format(item.ceiling),
        routeCode = item.routeCode,
        timezone = item.timezone,
        localeText = item.locale,
        quantity = item.quantity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerSlotViews(items: MutableList<LockerSlot>): MutableList<LockerSlotView> {
    val views = mutableListOf<LockerSlotView>()
    for (item in items) {
        views.add(toLockerSlotView(item))
    }
    return views
}

fun sumLockerSlotView(view: LockerSlotView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedFuelLiters + parsedCeiling + parsedQuantity.toDouble()
}
