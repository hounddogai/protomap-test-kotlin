package com.acme.transport.customs.tariff.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff estimate. */
data class TariffEstimateView(
    var code: String,
    var nameText: String,
    var regionText: String,
    var ceiling: String,
    var archived: String,
    var contactEmail: String,
    var threshold: String,
    var amount: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toTariffEstimateView(item: TariffEstimate): TariffEstimateView {
    return TariffEstimateView(
        code = item.code,
        nameText = item.name,
        regionText = truncate(item.region, 40),
        ceiling = "%.2f".format(item.ceiling),
        archived = "${item.archived}",
        contactEmail = item.contactEmail,
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffEstimateViews(items: MutableList<TariffEstimate>): MutableList<TariffEstimateView> {
    val views = mutableListOf<TariffEstimateView>()
    for (item in items) {
        views.add(toTariffEstimateView(item))
    }
    return views
}

fun sumTariffEstimateView(view: TariffEstimateView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedCeiling + parsedThreshold + parsedAmount
}
