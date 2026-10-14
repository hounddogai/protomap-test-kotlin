package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan entry. */
data class LanePlanEntryView(
    var code: String,
    var name: String,
    var checksumText: String,
    var stopCount: String,
    var trailerId: String,
    var maximum: String,
    var currency: String,
    var labelText: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toLanePlanEntryView(item: LanePlanEntry): LanePlanEntryView {
    return LanePlanEntryView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 80),
        checksumText = truncate(item.checksum, 120),
        stopCount = item.stopCount.toString(),
        trailerId = item.trailerId,
        maximum = "%.2f".format(item.maximum),
        currency = item.currency,
        labelText = truncate(item.label, 64),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanEntryViews(items: MutableList<LanePlanEntry>): MutableList<LanePlanEntryView> {
    val views = mutableListOf<LanePlanEntryView>()
    for (item in items) {
        views.add(toLanePlanEntryView(item))
    }
    return views
}

fun sumLanePlanEntryView(view: LanePlanEntryView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedStopCount.toDouble() + parsedMaximum + parsedSequence.toDouble()
}
