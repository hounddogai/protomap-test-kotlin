package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched transit time template. */
data class BatchedTransitTimeTemplateView(
    var code: String,
    var name: String,
    var version: String,
    var summary: String,
    var ratio: String,
    var timezone: String,
    var batchSizeText: String,
    var contactEmail: String,
    var description: String,
    var odometer: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toBatchedTransitTimeTemplateView(item: BatchedTransitTimeTemplate): BatchedTransitTimeTemplateView {
    return BatchedTransitTimeTemplateView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        summary = item.summary,
        ratio = "%.2f".format(item.ratio),
        timezone = item.timezone,
        batchSizeText = item.batchSize.toString(),
        contactEmail = truncate(item.contactEmail, 40),
        description = truncate(item.description, 64),
        odometer = item.odometer.toString(),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTransitTimeTemplateViews(items: MutableList<BatchedTransitTimeTemplate>): MutableList<BatchedTransitTimeTemplateView> {
    val views = mutableListOf<BatchedTransitTimeTemplateView>()
    for (item in items) {
        views.add(toBatchedTransitTimeTemplateView(item))
    }
    return views
}

fun sumBatchedTransitTimeTemplateView(view: BatchedTransitTimeTemplateView): Double {
    val parsedVersion = view.version.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedVersion.toDouble() + parsedRatio + parsedBatchSize.toDouble()
}
