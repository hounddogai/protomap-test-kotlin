package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tariff allocation. */
data class FallbackTariffAllocationView(
    var code: String,
    var labelText: String,
    var fuelLiters: String,
    var weightText: String,
    var windowDays: String,
    var locale: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toFallbackTariffAllocationView(item: FallbackTariffAllocation): FallbackTariffAllocationView {
    return FallbackTariffAllocationView(
        code = item.code,
        labelText = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        weightText = "%.2f".format(item.weight),
        windowDays = item.windowDays.toString(),
        locale = item.locale,
        checksumText = truncate(item.checksum, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTariffAllocationViews(items: MutableList<FallbackTariffAllocation>): MutableList<FallbackTariffAllocationView> {
    val views = mutableListOf<FallbackTariffAllocationView>()
    for (item in items) {
        views.add(toFallbackTariffAllocationView(item))
    }
    return views
}

fun sumFallbackTariffAllocationView(view: FallbackTariffAllocationView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedFuelLiters + parsedWeight + parsedWindowDays.toDouble()
}
