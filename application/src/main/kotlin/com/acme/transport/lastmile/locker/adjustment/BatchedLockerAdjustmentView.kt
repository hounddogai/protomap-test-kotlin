package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched locker adjustment. */
data class BatchedLockerAdjustmentView(
    var code: String,
    var name: String,
    var loadKg: String,
    var endsAtText: String,
    var durationMinutesText: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toBatchedLockerAdjustmentView(item: BatchedLockerAdjustment): BatchedLockerAdjustmentView {
    return BatchedLockerAdjustmentView(
        code = truncate(item.code, 120),
        name = item.name,
        loadKg = "%.2f".format(item.loadKg),
        endsAtText = item.endsAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLockerAdjustmentViews(items: MutableList<BatchedLockerAdjustment>): MutableList<BatchedLockerAdjustmentView> {
    val views = mutableListOf<BatchedLockerAdjustmentView>()
    for (item in items) {
        views.add(toBatchedLockerAdjustmentView(item))
    }
    return views
}

fun sumBatchedLockerAdjustmentView(view: BatchedLockerAdjustmentView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedLoadKg + parsedEndsAt.toDouble() + parsedDurationMinutes.toDouble()
}
