package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss threshold. */
data class NearMissThresholdView(
    var code: String,
    var title: String,
    var rate: String,
    var shiftCode: String,
    var sequenceText: String,
    var startsAtText: String,
    var unit: String,
    var statusLabel: String,
)

fun toNearMissThresholdView(item: NearMissThreshold): NearMissThresholdView {
    return NearMissThresholdView(
        code = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        shiftCode = truncate(item.shiftCode, 80),
        sequenceText = item.sequence.toString(),
        startsAtText = item.startsAt.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissThresholdViews(items: MutableList<NearMissThreshold>): MutableList<NearMissThresholdView> {
    val views = mutableListOf<NearMissThresholdView>()
    for (item in items) {
        views.add(toNearMissThresholdView(item))
    }
    return views
}

fun sumNearMissThresholdView(view: NearMissThresholdView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedRate + parsedSequence.toDouble() + parsedStartsAt.toDouble()
}
