package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default sortation record. */
data class DefaultSortationRecordView(
    var code: String,
    var label: String,
    var capacity: String,
    var revision: String,
    var batchSize: String,
    var amount: String,
    var statusLabel: String,
)

fun toDefaultSortationRecordView(item: DefaultSortationRecord): DefaultSortationRecordView {
    return DefaultSortationRecordView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 80),
        capacity = item.capacity.toString(),
        revision = item.revision.toString(),
        batchSize = item.batchSize.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultSortationRecordViews(items: MutableList<DefaultSortationRecord>): MutableList<DefaultSortationRecordView> {
    val views = mutableListOf<DefaultSortationRecordView>()
    for (item in items) {
        views.add(toDefaultSortationRecordView(item))
    }
    return views
}

fun sumDefaultSortationRecordView(view: DefaultSortationRecordView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedCapacity.toDouble() + parsedRevision.toDouble() + parsedBatchSize.toDouble()
}
