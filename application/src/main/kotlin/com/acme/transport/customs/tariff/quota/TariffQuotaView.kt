package com.acme.transport.customs.tariff.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff quota. */
data class TariffQuotaView(
    var codeText: String,
    var name: String,
    var fuelLiters: String,
    var shiftCode: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTariffQuotaView(item: TariffQuota): TariffQuotaView {
    return TariffQuotaView(
        codeText = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        shiftCode = truncate(item.shiftCode, 64),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffQuotaViews(items: MutableList<TariffQuota>): MutableList<TariffQuotaView> {
    val views = mutableListOf<TariffQuotaView>()
    for (item in items) {
        views.add(toTariffQuotaView(item))
    }
    return views
}

fun sumTariffQuotaView(view: TariffQuotaView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedFuelLiters + parsedCapacity.toDouble()
}
