package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict locker target. */
data class StrictLockerTargetView(
    var code: String,
    var title: String,
    var batchSize: String,
    var capacity: String,
    var unit: String,
    var sequence: String,
    var statusLabel: String,
)

fun toStrictLockerTargetView(item: StrictLockerTarget): StrictLockerTargetView {
    return StrictLockerTargetView(
        code = item.code,
        title = item.title,
        batchSize = item.batchSize.toString(),
        capacity = item.capacity.toString(),
        unit = truncate(item.unit, 64),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictLockerTargetViews(items: MutableList<StrictLockerTarget>): MutableList<StrictLockerTargetView> {
    val views = mutableListOf<StrictLockerTargetView>()
    for (item in items) {
        views.add(toStrictLockerTargetView(item))
    }
    return views
}

fun sumStrictLockerTargetView(view: StrictLockerTargetView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedBatchSize.toDouble() + parsedCapacity.toDouble() + parsedSequence.toDouble()
}
