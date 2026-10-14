package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tariff batch. */
data class DefaultTariffBatchView(
    var code: String,
    var titleText: String,
    var region: String,
    var checksumText: String,
    var trailerIdText: String,
    var budget: String,
    var startsAt: String,
    var maximumText: String,
    var discountText: String,
    var statusLabel: String,
)

fun toDefaultTariffBatchView(item: DefaultTariffBatch): DefaultTariffBatchView {
    return DefaultTariffBatchView(
        code = truncate(item.code, 40),
        titleText = item.title,
        region = item.region,
        checksumText = truncate(item.checksum, 64),
        trailerIdText = item.trailerId,
        budget = "%.2f".format(item.budget),
        startsAt = item.startsAt.toString(),
        maximumText = "%.2f".format(item.maximum),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTariffBatchViews(items: MutableList<DefaultTariffBatch>): MutableList<DefaultTariffBatchView> {
    val views = mutableListOf<DefaultTariffBatchView>()
    for (item in items) {
        views.add(toDefaultTariffBatchView(item))
    }
    return views
}

fun sumDefaultTariffBatchView(view: DefaultTariffBatchView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedBudget + parsedStartsAt.toDouble() + parsedMaximum
}
