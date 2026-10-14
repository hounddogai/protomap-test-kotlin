package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tender checkpoint. */
data class LenientTenderCheckpointView(
    var code: String,
    var label: String,
    var updatedAtText: String,
    var minimum: String,
    var amount: String,
    var retries: String,
    var archived: String,
    var carrier: String,
    var quantityText: String,
    var rate: String,
    var statusLabel: String,
)

fun toLenientTenderCheckpointView(item: LenientTenderCheckpoint): LenientTenderCheckpointView {
    return LenientTenderCheckpointView(
        code = item.code,
        label = truncate(item.label, 40),
        updatedAtText = item.updatedAt.toString(),
        minimum = "%.2f".format(item.minimum),
        amount = "%.2f".format(item.amount),
        retries = item.retries.toString(),
        archived = "${item.archived}",
        carrier = item.carrier,
        quantityText = item.quantity.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTenderCheckpointViews(items: MutableList<LenientTenderCheckpoint>): MutableList<LenientTenderCheckpointView> {
    val views = mutableListOf<LenientTenderCheckpointView>()
    for (item in items) {
        views.add(toLenientTenderCheckpointView(item))
    }
    return views
}

fun sumLenientTenderCheckpointView(view: LenientTenderCheckpointView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedUpdatedAt.toDouble() + parsedMinimum + parsedAmount
}
