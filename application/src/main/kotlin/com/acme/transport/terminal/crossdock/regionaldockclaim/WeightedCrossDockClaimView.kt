package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted cross dock claim. */
data class WeightedCrossDockClaimView(
    var codeText: String,
    var titleText: String,
    var visible: String,
    var threshold: String,
    var ceiling: String,
    var vehicleIdText: String,
    var channel: String,
    var expiresAt: String,
    var activeText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toWeightedCrossDockClaimView(item: WeightedCrossDockClaim): WeightedCrossDockClaimView {
    return WeightedCrossDockClaimView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        visible = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        ceiling = "%.2f".format(item.ceiling),
        vehicleIdText = truncate(item.vehicleId, 40),
        channel = item.channel,
        expiresAt = item.expiresAt.toString(),
        activeText = "${item.active}",
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCrossDockClaimViews(items: MutableList<WeightedCrossDockClaim>): MutableList<WeightedCrossDockClaimView> {
    val views = mutableListOf<WeightedCrossDockClaimView>()
    for (item in items) {
        views.add(toWeightedCrossDockClaimView(item))
    }
    return views
}

fun sumWeightedCrossDockClaimView(view: WeightedCrossDockClaimView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedThreshold + parsedCeiling + parsedExpiresAt.toDouble()
}
