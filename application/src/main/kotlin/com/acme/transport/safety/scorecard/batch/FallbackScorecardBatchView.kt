package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback scorecard batch. */
data class FallbackScorecardBatchView(
    var code: String,
    var label: String,
    var pallets: String,
    var driverId: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toFallbackScorecardBatchView(item: FallbackScorecardBatch): FallbackScorecardBatchView {
    return FallbackScorecardBatchView(
        code = item.code,
        label = truncate(item.label, 40),
        pallets = item.pallets.toString(),
        driverId = truncate(item.driverId, 64),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackScorecardBatchViews(items: MutableList<FallbackScorecardBatch>): MutableList<FallbackScorecardBatchView> {
    val views = mutableListOf<FallbackScorecardBatchView>()
    for (item in items) {
        views.add(toFallbackScorecardBatchView(item))
    }
    return views
}

fun sumFallbackScorecardBatchView(view: FallbackScorecardBatchView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
