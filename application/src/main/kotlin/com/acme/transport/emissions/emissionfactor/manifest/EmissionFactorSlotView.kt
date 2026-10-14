package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor slot. */
data class EmissionFactorSlotView(
    var code: String,
    var title: String,
    var depotText: String,
    var pinned: String,
    var lane: String,
    var fuelLiters: String,
    var amount: String,
    var durationMinutes: String,
    var cost: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toEmissionFactorSlotView(item: EmissionFactorSlot): EmissionFactorSlotView {
    return EmissionFactorSlotView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 40),
        depotText = truncate(item.depot, 120),
        pinned = "${item.pinned}",
        lane = truncate(item.lane, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        amount = "%.2f".format(item.amount),
        durationMinutes = item.durationMinutes.toString(),
        cost = "%.2f".format(item.cost),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorSlotViews(items: MutableList<EmissionFactorSlot>): MutableList<EmissionFactorSlotView> {
    val views = mutableListOf<EmissionFactorSlotView>()
    for (item in items) {
        views.add(toEmissionFactorSlotView(item))
    }
    return views
}

fun sumEmissionFactorSlotView(view: EmissionFactorSlotView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedFuelLiters + parsedAmount + parsedDurationMinutes.toDouble()
}
