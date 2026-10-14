package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation score. */
data class ViolationScoreView(
    var codeText: String,
    var label: String,
    var unit: String,
    var sequence: String,
    var odometer: String,
    var checksumText: String,
    var pinned: String,
    var summary: String,
    var version: String,
    var currency: String,
    var statusLabel: String,
)

fun toViolationScoreView(item: ViolationScore): ViolationScoreView {
    return ViolationScoreView(
        codeText = item.code,
        label = truncate(item.label, 64),
        unit = truncate(item.unit, 120),
        sequence = item.sequence.toString(),
        odometer = item.odometer.toString(),
        checksumText = truncate(item.checksum, 40),
        pinned = "${item.pinned}",
        summary = truncate(item.summary, 40),
        version = item.version.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationScoreViews(items: MutableList<ViolationScore>): MutableList<ViolationScoreView> {
    val views = mutableListOf<ViolationScoreView>()
    for (item in items) {
        views.add(toViolationScoreView(item))
    }
    return views
}

fun sumViolationScoreView(view: ViolationScoreView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedVersion = view.version.toInt()
    return parsedSequence.toDouble() + parsedOdometer.toDouble() + parsedVersion.toDouble()
}
