package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback violation adjustment. */
data class FallbackViolationAdjustmentView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var contactEmail: String,
    var durationMinutes: String,
    var minimumText: String,
    var version: String,
    var channelText: String,
    var reviewer: String,
    var description: String,
    var statusLabel: String,
)

fun toFallbackViolationAdjustmentView(item: FallbackViolationAdjustment): FallbackViolationAdjustmentView {
    return FallbackViolationAdjustmentView(
        code = item.code,
        labelText = truncate(item.label, 120),
        ratio = "%.2f".format(item.ratio),
        contactEmail = item.contactEmail,
        durationMinutes = item.durationMinutes.toString(),
        minimumText = "%.2f".format(item.minimum),
        version = item.version.toString(),
        channelText = item.channel,
        reviewer = item.reviewer,
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackViolationAdjustmentViews(items: MutableList<FallbackViolationAdjustment>): MutableList<FallbackViolationAdjustmentView> {
    val views = mutableListOf<FallbackViolationAdjustmentView>()
    for (item in items) {
        views.add(toFallbackViolationAdjustmentView(item))
    }
    return views
}

fun sumFallbackViolationAdjustmentView(view: FallbackViolationAdjustmentView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRatio + parsedDurationMinutes.toDouble() + parsedMinimum
}
