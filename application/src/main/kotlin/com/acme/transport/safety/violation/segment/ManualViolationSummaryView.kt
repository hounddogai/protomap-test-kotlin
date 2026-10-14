package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual violation summary. */
data class ManualViolationSummaryView(
    var code: String,
    var name: String,
    var locale: String,
    var expiresAt: String,
    var shiftCode: String,
    var threshold: String,
    var vehicleIdText: String,
    var summary: String,
    var loadKgText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toManualViolationSummaryView(item: ManualViolationSummary): ManualViolationSummaryView {
    return ManualViolationSummaryView(
        code = item.code,
        name = item.name,
        locale = item.locale,
        expiresAt = item.expiresAt.toString(),
        shiftCode = truncate(item.shiftCode, 120),
        threshold = "%.2f".format(item.threshold),
        vehicleIdText = item.vehicleId,
        summary = item.summary,
        loadKgText = "%.2f".format(item.loadKg),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualViolationSummaryViews(items: MutableList<ManualViolationSummary>): MutableList<ManualViolationSummaryView> {
    val views = mutableListOf<ManualViolationSummaryView>()
    for (item in items) {
        views.add(toManualViolationSummaryView(item))
    }
    return views
}

fun sumManualViolationSummaryView(view: ManualViolationSummaryView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedExpiresAt.toDouble() + parsedThreshold + parsedLoadKg
}
