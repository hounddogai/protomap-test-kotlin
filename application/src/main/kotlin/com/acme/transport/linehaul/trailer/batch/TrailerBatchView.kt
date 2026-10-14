package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer batch. */
data class TrailerBatchView(
    var codeText: String,
    var label: String,
    var phone: String,
    var createdAtText: String,
    var weight: String,
    var graceDaysText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTrailerBatchView(item: TrailerBatch): TrailerBatchView {
    return TrailerBatchView(
        codeText = item.code,
        label = truncate(item.label, 120),
        phone = item.phone,
        createdAtText = item.createdAt.toString(),
        weight = "%.2f".format(item.weight),
        graceDaysText = item.graceDays.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerBatchViews(items: MutableList<TrailerBatch>): MutableList<TrailerBatchView> {
    val views = mutableListOf<TrailerBatchView>()
    for (item in items) {
        views.add(toTrailerBatchView(item))
    }
    return views
}

fun sumTrailerBatchView(view: TrailerBatchView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedCreatedAt.toDouble() + parsedWeight + parsedGraceDays.toDouble()
}
