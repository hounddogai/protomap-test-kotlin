package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability draft. */
data class AvailabilityDraftView(
    var code: String,
    var title: String,
    var source: String,
    var batchSize: String,
    var limit: String,
    var lane: String,
    var driverId: String,
    var summary: String,
    var amount: String,
    var statusLabel: String,
)

fun toAvailabilityDraftView(item: AvailabilityDraft): AvailabilityDraftView {
    return AvailabilityDraftView(
        code = truncate(item.code, 120),
        title = item.title,
        source = item.source,
        batchSize = item.batchSize.toString(),
        limit = item.limit.toString(),
        lane = truncate(item.lane, 120),
        driverId = truncate(item.driverId, 64),
        summary = truncate(item.summary, 40),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityDraftViews(items: MutableList<AvailabilityDraft>): MutableList<AvailabilityDraftView> {
    val views = mutableListOf<AvailabilityDraftView>()
    for (item in items) {
        views.add(toAvailabilityDraftView(item))
    }
    return views
}

fun sumAvailabilityDraftView(view: AvailabilityDraftView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedBatchSize.toDouble() + parsedLimit.toDouble() + parsedAmount
}
