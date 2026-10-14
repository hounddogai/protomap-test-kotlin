package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal clearance notice. */
data class SeasonalClearanceNoticeView(
    var code: String,
    var title: String,
    var threshold: String,
    var summary: String,
    var batchSizeText: String,
    var vehicleId: String,
    var updatedAtText: String,
    var etaMinutes: String,
    var segment: String,
    var statusLabel: String,
)

fun toSeasonalClearanceNoticeView(item: SeasonalClearanceNotice): SeasonalClearanceNoticeView {
    return SeasonalClearanceNoticeView(
        code = item.code,
        title = truncate(item.title, 40),
        threshold = "%.2f".format(item.threshold),
        summary = item.summary,
        batchSizeText = item.batchSize.toString(),
        vehicleId = item.vehicleId,
        updatedAtText = item.updatedAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalClearanceNoticeViews(items: MutableList<SeasonalClearanceNotice>): MutableList<SeasonalClearanceNoticeView> {
    val views = mutableListOf<SeasonalClearanceNoticeView>()
    for (item in items) {
        views.add(toSeasonalClearanceNoticeView(item))
    }
    return views
}

fun sumSeasonalClearanceNoticeView(view: SeasonalClearanceNoticeView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedThreshold + parsedBatchSize.toDouble() + parsedUpdatedAt.toDouble()
}
