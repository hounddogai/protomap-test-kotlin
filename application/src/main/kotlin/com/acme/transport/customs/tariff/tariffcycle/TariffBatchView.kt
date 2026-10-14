package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff batch. */
data class TariffBatchView(
    var code: String,
    var titleText: String,
    var timezone: String,
    var priorityText: String,
    var trailerId: String,
    var attempts: String,
    var summary: String,
    var quantity: String,
    var batchSize: String,
    var carrier: String,
    var driverId: String,
    var statusLabel: String,
)

fun toTariffBatchView(item: TariffBatch): TariffBatchView {
    return TariffBatchView(
        code = item.code,
        titleText = item.title,
        timezone = truncate(item.timezone, 64),
        priorityText = item.priority.toString(),
        trailerId = truncate(item.trailerId, 40),
        attempts = item.attempts.toString(),
        summary = truncate(item.summary, 120),
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        carrier = truncate(item.carrier, 120),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffBatchViews(items: MutableList<TariffBatch>): MutableList<TariffBatchView> {
    val views = mutableListOf<TariffBatchView>()
    for (item in items) {
        views.add(toTariffBatchView(item))
    }
    return views
}

fun sumTariffBatchView(view: TariffBatchView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedPriority.toDouble() + parsedAttempts.toDouble() + parsedQuantity.toDouble()
}
