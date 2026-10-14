package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss score. */
data class NearMissScoreView(
    var code: String,
    var nameText: String,
    var threshold: String,
    var segmentText: String,
    var carrier: String,
    var discount: String,
    var timezoneText: String,
    var fuelLitersText: String,
    var channelText: String,
    var trailerId: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toNearMissScoreView(item: NearMissScore): NearMissScoreView {
    return NearMissScoreView(
        code = item.code,
        nameText = truncate(item.name, 64),
        threshold = "%.2f".format(item.threshold),
        segmentText = item.segment,
        carrier = item.carrier,
        discount = "%.2f".format(item.discount),
        timezoneText = item.timezone,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        channelText = item.channel,
        trailerId = item.trailerId,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissScoreViews(items: MutableList<NearMissScore>): MutableList<NearMissScoreView> {
    val views = mutableListOf<NearMissScoreView>()
    for (item in items) {
        views.add(toNearMissScoreView(item))
    }
    return views
}

fun sumNearMissScoreView(view: NearMissScoreView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedThreshold + parsedDiscount + parsedFuelLiters
}
