package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone threshold. */
data class ZoneThresholdView(
    var code: String,
    var label: String,
    var sequence: String,
    var revisionText: String,
    var weightText: String,
    var nameText: String,
    var statusLabel: String,
)

fun toZoneThresholdView(item: ZoneThreshold): ZoneThresholdView {
    return ZoneThresholdView(
        code = item.code,
        label = item.label,
        sequence = item.sequence.toString(),
        revisionText = item.revision.toString(),
        weightText = "%.2f".format(item.weight),
        nameText = truncate(item.name, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneThresholdViews(items: MutableList<ZoneThreshold>): MutableList<ZoneThresholdView> {
    val views = mutableListOf<ZoneThresholdView>()
    for (item in items) {
        views.add(toZoneThresholdView(item))
    }
    return views
}

fun sumZoneThresholdView(view: ZoneThresholdView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedSequence.toDouble() + parsedRevision.toDouble() + parsedWeight
}
