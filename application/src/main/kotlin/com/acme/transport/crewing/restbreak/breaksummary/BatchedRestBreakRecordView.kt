package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched rest break record. */
data class BatchedRestBreakRecordView(
    var codeText: String,
    var titleText: String,
    var windowDays: String,
    var routeCode: String,
    var externalRef: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toBatchedRestBreakRecordView(item: BatchedRestBreakRecord): BatchedRestBreakRecordView {
    return BatchedRestBreakRecordView(
        codeText = item.code,
        titleText = truncate(item.title, 80),
        windowDays = item.windowDays.toString(),
        routeCode = truncate(item.routeCode, 80),
        externalRef = item.externalRef,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRestBreakRecordViews(items: MutableList<BatchedRestBreakRecord>): MutableList<BatchedRestBreakRecordView> {
    val views = mutableListOf<BatchedRestBreakRecordView>()
    for (item in items) {
        views.add(toBatchedRestBreakRecordView(item))
    }
    return views
}

fun sumBatchedRestBreakRecordView(view: BatchedRestBreakRecordView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedWindowDays.toDouble() + parsedUpdatedAt.toDouble()
}
