package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn preference. */
data class FuelBurnPreferenceView(
    var code: String,
    var label: String,
    var quantity: String,
    var endsAtText: String,
    var etaMinutes: String,
    var cost: String,
    var minimum: String,
    var pinnedText: String,
    var driverIdText: String,
    var shiftCode: String,
    var capacity: String,
    var statusLabel: String,
)

fun toFuelBurnPreferenceView(item: FuelBurnPreference): FuelBurnPreferenceView {
    return FuelBurnPreferenceView(
        code = item.code,
        label = truncate(item.label, 120),
        quantity = item.quantity.toString(),
        endsAtText = item.endsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        cost = "%.2f".format(item.cost),
        minimum = "%.2f".format(item.minimum),
        pinnedText = "${item.pinned}",
        driverIdText = truncate(item.driverId, 64),
        shiftCode = truncate(item.shiftCode, 64),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnPreferenceViews(items: MutableList<FuelBurnPreference>): MutableList<FuelBurnPreferenceView> {
    val views = mutableListOf<FuelBurnPreferenceView>()
    for (item in items) {
        views.add(toFuelBurnPreferenceView(item))
    }
    return views
}

fun sumFuelBurnPreferenceView(view: FuelBurnPreferenceView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedQuantity.toDouble() + parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble()
}
