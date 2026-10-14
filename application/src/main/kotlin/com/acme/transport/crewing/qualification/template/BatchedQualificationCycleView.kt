package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched qualification cycle. */
data class BatchedQualificationCycleView(
    var code: String,
    var title: String,
    var graceDays: String,
    var startsAt: String,
    var createdAt: String,
    var trailerId: String,
    var phone: String,
    var weightText: String,
    var nameText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toBatchedQualificationCycleView(item: BatchedQualificationCycle): BatchedQualificationCycleView {
    return BatchedQualificationCycleView(
        code = truncate(item.code, 120),
        title = item.title,
        graceDays = item.graceDays.toString(),
        startsAt = item.startsAt.toString(),
        createdAt = item.createdAt.toString(),
        trailerId = item.trailerId,
        phone = truncate(item.phone, 120),
        weightText = "%.2f".format(item.weight),
        nameText = truncate(item.name, 120),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedQualificationCycleViews(items: MutableList<BatchedQualificationCycle>): MutableList<BatchedQualificationCycleView> {
    val views = mutableListOf<BatchedQualificationCycleView>()
    for (item in items) {
        views.add(toBatchedQualificationCycleView(item))
    }
    return views
}

fun sumBatchedQualificationCycleView(view: BatchedQualificationCycleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedGraceDays.toDouble() + parsedStartsAt.toDouble() + parsedCreatedAt.toDouble()
}
