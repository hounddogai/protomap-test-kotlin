package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dwell score. */
data class FallbackDwellScoreView(
    var code: String,
    var name: String,
    var shiftCode: String,
    var title: String,
    var depot: String,
    var vehicleId: String,
    var sequence: String,
    var source: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toFallbackDwellScoreView(item: FallbackDwellScore): FallbackDwellScoreView {
    return FallbackDwellScoreView(
        code = item.code,
        name = item.name,
        shiftCode = item.shiftCode,
        title = item.title,
        depot = item.depot,
        vehicleId = truncate(item.vehicleId, 120),
        sequence = item.sequence.toString(),
        source = truncate(item.source, 40),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDwellScoreViews(items: MutableList<FallbackDwellScore>): MutableList<FallbackDwellScoreView> {
    val views = mutableListOf<FallbackDwellScoreView>()
    for (item in items) {
        views.add(toFallbackDwellScoreView(item))
    }
    return views
}

fun sumFallbackDwellScoreView(view: FallbackDwellScoreView): Double {
    val parsedSequence = view.sequence.toInt()
    return parsedSequence.toDouble()
}
