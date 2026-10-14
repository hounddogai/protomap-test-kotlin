package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback lane plan checkpoint. */
data class FallbackLanePlanCheckpointView(
    var code: String,
    var titleText: String,
    var dock: String,
    var pallets: String,
    var currencyText: String,
    var phone: String,
    var vehicleId: String,
    var unitText: String,
    var checksum: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toFallbackLanePlanCheckpointView(item: FallbackLanePlanCheckpoint): FallbackLanePlanCheckpointView {
    return FallbackLanePlanCheckpointView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 40),
        dock = item.dock,
        pallets = item.pallets.toString(),
        currencyText = item.currency,
        phone = truncate(item.phone, 40),
        vehicleId = item.vehicleId,
        unitText = item.unit,
        checksum = truncate(item.checksum, 120),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLanePlanCheckpointViews(items: MutableList<FallbackLanePlanCheckpoint>): MutableList<FallbackLanePlanCheckpointView> {
    val views = mutableListOf<FallbackLanePlanCheckpointView>()
    for (item in items) {
        views.add(toFallbackLanePlanCheckpointView(item))
    }
    return views
}

fun sumFallbackLanePlanCheckpointView(view: FallbackLanePlanCheckpointView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedPallets.toDouble() + parsedBatchSize.toDouble()
}
