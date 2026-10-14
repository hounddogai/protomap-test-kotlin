package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy cutoff batch. */
data class LegacyCutoffBatchView(
    var codeText: String,
    var titleText: String,
    var floorValueText: String,
    var checksumText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toLegacyCutoffBatchView(item: LegacyCutoffBatch): LegacyCutoffBatchView {
    return LegacyCutoffBatchView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        floorValueText = "%.2f".format(item.floorValue),
        checksumText = item.checksum,
        currencyText = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCutoffBatchViews(items: MutableList<LegacyCutoffBatch>): MutableList<LegacyCutoffBatchView> {
    val views = mutableListOf<LegacyCutoffBatchView>()
    for (item in items) {
        views.add(toLegacyCutoffBatchView(item))
    }
    return views
}

fun sumLegacyCutoffBatchView(view: LegacyCutoffBatchView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedFloorValue
}
