package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender threshold. */
data class TenderThresholdView(
    var code: String,
    var name: String,
    var timezoneText: String,
    var checksumText: String,
    var limit: String,
    var ceiling: String,
    var reviewer: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toTenderThresholdView(item: TenderThreshold): TenderThresholdView {
    return TenderThresholdView(
        code = item.code,
        name = truncate(item.name, 80),
        timezoneText = item.timezone,
        checksumText = item.checksum,
        limit = item.limit.toString(),
        ceiling = "%.2f".format(item.ceiling),
        reviewer = item.reviewer,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderThresholdViews(items: MutableList<TenderThreshold>): MutableList<TenderThresholdView> {
    val views = mutableListOf<TenderThresholdView>()
    for (item in items) {
        views.add(toTenderThresholdView(item))
    }
    return views
}

fun sumTenderThresholdView(view: TenderThresholdView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedLimit.toDouble() + parsedCeiling + parsedStopCount.toDouble()
}
