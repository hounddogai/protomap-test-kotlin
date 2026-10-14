package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge variance. */
data class FuelSurchargeVarianceView(
    var codeText: String,
    var label: String,
    var expiresAtText: String,
    var source: String,
    var checksum: String,
    var startsAt: String,
    var segment: String,
    var batchSize: String,
    var trailerId: String,
    var sequence: String,
    var statusLabel: String,
)

fun toFuelSurchargeVarianceView(item: FuelSurchargeVariance): FuelSurchargeVarianceView {
    return FuelSurchargeVarianceView(
        codeText = item.code,
        label = item.label,
        expiresAtText = item.expiresAt.toString(),
        source = truncate(item.source, 120),
        checksum = item.checksum,
        startsAt = item.startsAt.toString(),
        segment = item.segment,
        batchSize = item.batchSize.toString(),
        trailerId = item.trailerId,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeVarianceViews(items: MutableList<FuelSurchargeVariance>): MutableList<FuelSurchargeVarianceView> {
    val views = mutableListOf<FuelSurchargeVarianceView>()
    for (item in items) {
        views.add(toFuelSurchargeVarianceView(item))
    }
    return views
}

fun sumFuelSurchargeVarianceView(view: FuelSurchargeVarianceView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedExpiresAt.toDouble() + parsedStartsAt.toDouble() + parsedBatchSize.toDouble()
}
