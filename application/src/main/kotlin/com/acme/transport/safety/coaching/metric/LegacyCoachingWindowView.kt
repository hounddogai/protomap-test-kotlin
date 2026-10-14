package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy coaching window. */
data class LegacyCoachingWindowView(
    var code: String,
    var labelText: String,
    var limit: String,
    var batchSize: String,
    var phoneText: String,
    var marginText: String,
    var vehicleIdText: String,
    var fuelLitersText: String,
    var source: String,
    var endsAt: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toLegacyCoachingWindowView(item: LegacyCoachingWindow): LegacyCoachingWindowView {
    return LegacyCoachingWindowView(
        code = item.code,
        labelText = truncate(item.label, 80),
        limit = item.limit.toString(),
        batchSize = item.batchSize.toString(),
        phoneText = item.phone,
        marginText = "%.2f".format(item.margin),
        vehicleIdText = truncate(item.vehicleId, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        source = item.source,
        endsAt = item.endsAt.toString(),
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCoachingWindowViews(items: MutableList<LegacyCoachingWindow>): MutableList<LegacyCoachingWindowView> {
    val views = mutableListOf<LegacyCoachingWindowView>()
    for (item in items) {
        views.add(toLegacyCoachingWindowView(item))
    }
    return views
}

fun sumLegacyCoachingWindowView(view: LegacyCoachingWindowView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedLimit.toDouble() + parsedBatchSize.toDouble() + parsedMargin
}
