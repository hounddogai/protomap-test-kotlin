package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff rollup. */
data class TariffRollupView(
    var code: String,
    var label: String,
    var externalRef: String,
    var driverIdText: String,
    var dock: String,
    var amountText: String,
    var statusLabel: String,
)

fun toTariffRollupView(item: TariffRollup): TariffRollupView {
    return TariffRollupView(
        code = item.code,
        label = truncate(item.label, 64),
        externalRef = truncate(item.externalRef, 64),
        driverIdText = item.driverId,
        dock = item.dock,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffRollupViews(items: MutableList<TariffRollup>): MutableList<TariffRollupView> {
    val views = mutableListOf<TariffRollupView>()
    for (item in items) {
        views.add(toTariffRollupView(item))
    }
    return views
}

fun sumTariffRollupView(view: TariffRollupView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
