package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff allocation. */
data class TariffAllocationView(
    var code: String,
    var title: String,
    var routeCode: String,
    var externalRef: String,
    var durationMinutesText: String,
    var vehicleIdText: String,
    var amountText: String,
    var ratioText: String,
    var batchSize: String,
    var graceDaysText: String,
    var margin: String,
    var statusLabel: String,
)

fun toTariffAllocationView(item: TariffAllocation): TariffAllocationView {
    return TariffAllocationView(
        code = truncate(item.code, 80),
        title = item.title,
        routeCode = truncate(item.routeCode, 40),
        externalRef = truncate(item.externalRef, 80),
        durationMinutesText = item.durationMinutes.toString(),
        vehicleIdText = item.vehicleId,
        amountText = "%.2f".format(item.amount),
        ratioText = "%.2f".format(item.ratio),
        batchSize = item.batchSize.toString(),
        graceDaysText = item.graceDays.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffAllocationViews(items: MutableList<TariffAllocation>): MutableList<TariffAllocationView> {
    val views = mutableListOf<TariffAllocationView>()
    for (item in items) {
        views.add(toTariffAllocationView(item))
    }
    return views
}

fun sumTariffAllocationView(view: TariffAllocationView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedAmount + parsedRatio
}
