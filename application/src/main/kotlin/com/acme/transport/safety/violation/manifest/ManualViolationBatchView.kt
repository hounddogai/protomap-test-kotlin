package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual violation batch. */
data class ManualViolationBatchView(
    var code: String,
    var label: String,
    var discount: String,
    var routeCode: String,
    var archived: String,
    var batchSize: String,
    var timezone: String,
    var statusLabel: String,
)

fun toManualViolationBatchView(item: ManualViolationBatch): ManualViolationBatchView {
    return ManualViolationBatchView(
        code = item.code,
        label = truncate(item.label, 64),
        discount = "%.2f".format(item.discount),
        routeCode = truncate(item.routeCode, 64),
        archived = "${item.archived}",
        batchSize = item.batchSize.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualViolationBatchViews(items: MutableList<ManualViolationBatch>): MutableList<ManualViolationBatchView> {
    val views = mutableListOf<ManualViolationBatchView>()
    for (item in items) {
        views.add(toManualViolationBatchView(item))
    }
    return views
}

fun sumManualViolationBatchView(view: ManualViolationBatchView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDiscount + parsedBatchSize.toDouble()
}
