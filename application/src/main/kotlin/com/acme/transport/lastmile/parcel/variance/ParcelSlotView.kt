package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel slot. */
data class ParcelSlotView(
    var code: String,
    var label: String,
    var currency: String,
    var weight: String,
    var capacityText: String,
    var active: String,
    var locale: String,
    var notesText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toParcelSlotView(item: ParcelSlot): ParcelSlotView {
    return ParcelSlotView(
        code = truncate(item.code, 64),
        label = item.label,
        currency = truncate(item.currency, 120),
        weight = "%.2f".format(item.weight),
        capacityText = item.capacity.toString(),
        active = "${item.active}",
        locale = item.locale,
        notesText = truncate(item.notes, 80),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelSlotViews(items: MutableList<ParcelSlot>): MutableList<ParcelSlotView> {
    val views = mutableListOf<ParcelSlotView>()
    for (item in items) {
        views.add(toParcelSlotView(item))
    }
    return views
}

fun sumParcelSlotView(view: ParcelSlotView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedWeight + parsedCapacity.toDouble() + parsedBatchSize.toDouble()
}
