package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff budget. */
data class TariffBudgetView(
    var code: String,
    var name: String,
    var currency: String,
    var locale: String,
    var createdAtText: String,
    var unit: String,
    var stopCountText: String,
    var laneText: String,
    var statusLabel: String,
)

fun toTariffBudgetView(item: TariffBudget): TariffBudgetView {
    return TariffBudgetView(
        code = item.code,
        name = item.name,
        currency = item.currency,
        locale = truncate(item.locale, 80),
        createdAtText = item.createdAt.toString(),
        unit = item.unit,
        stopCountText = item.stopCount.toString(),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffBudgetViews(items: MutableList<TariffBudget>): MutableList<TariffBudgetView> {
    val views = mutableListOf<TariffBudgetView>()
    for (item in items) {
        views.add(toTariffBudgetView(item))
    }
    return views
}

fun sumTariffBudgetView(view: TariffBudgetView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}
