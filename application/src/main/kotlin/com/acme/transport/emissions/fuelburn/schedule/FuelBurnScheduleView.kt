package com.acme.transport.emissions.fuelburn.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn schedule. */
data class FuelBurnScheduleView(
    var code: String,
    var label: String,
    var startsAt: String,
    var routeCode: String,
    var etaMinutes: String,
    var description: String,
    var unit: String,
    var visible: String,
    var discount: String,
    var statusLabel: String,
)

fun toFuelBurnScheduleView(item: FuelBurnSchedule): FuelBurnScheduleView {
    return FuelBurnScheduleView(
        code = item.code,
        label = item.label,
        startsAt = item.startsAt.toString(),
        routeCode = item.routeCode,
        etaMinutes = item.etaMinutes.toString(),
        description = item.description,
        unit = truncate(item.unit, 80),
        visible = "${item.visible}",
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnScheduleViews(items: MutableList<FuelBurnSchedule>): MutableList<FuelBurnScheduleView> {
    val views = mutableListOf<FuelBurnScheduleView>()
    for (item in items) {
        views.add(toFuelBurnScheduleView(item))
    }
    return views
}

fun sumFuelBurnScheduleView(view: FuelBurnScheduleView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedStartsAt.toDouble() + parsedEtaMinutes.toDouble() + parsedDiscount
}
