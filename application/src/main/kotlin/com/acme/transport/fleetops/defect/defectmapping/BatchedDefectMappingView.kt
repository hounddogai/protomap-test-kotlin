package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched defect mapping. */
data class BatchedDefectMappingView(
    var codeText: String,
    var title: String,
    var archived: String,
    var reviewer: String,
    var lane: String,
    var rate: String,
    var active: String,
    var phone: String,
    var trailerIdText: String,
    var expiresAt: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toBatchedDefectMappingView(item: BatchedDefectMapping): BatchedDefectMappingView {
    return BatchedDefectMappingView(
        codeText = item.code,
        title = truncate(item.title, 120),
        archived = "${item.archived}",
        reviewer = item.reviewer,
        lane = item.lane,
        rate = "%.2f".format(item.rate),
        active = "${item.active}",
        phone = item.phone,
        trailerIdText = item.trailerId,
        expiresAt = item.expiresAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDefectMappingViews(items: MutableList<BatchedDefectMapping>): MutableList<BatchedDefectMappingView> {
    val views = mutableListOf<BatchedDefectMappingView>()
    for (item in items) {
        views.add(toBatchedDefectMappingView(item))
    }
    return views
}

fun sumBatchedDefectMappingView(view: BatchedDefectMappingView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRate + parsedExpiresAt.toDouble() + parsedWindowDays.toDouble()
}
