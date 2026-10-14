package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge checkpoint. */
data class EvChargeCheckpointView(
    var codeText: String,
    var title: String,
    var quantityText: String,
    var pallets: String,
    var label: String,
    var carrier: String,
    var statusLabel: String,
)

fun toEvChargeCheckpointView(item: EvChargeCheckpoint): EvChargeCheckpointView {
    return EvChargeCheckpointView(
        codeText = item.code,
        title = truncate(item.title, 40),
        quantityText = item.quantity.toString(),
        pallets = item.pallets.toString(),
        label = item.label,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeCheckpointViews(items: MutableList<EvChargeCheckpoint>): MutableList<EvChargeCheckpointView> {
    val views = mutableListOf<EvChargeCheckpointView>()
    for (item in items) {
        views.add(toEvChargeCheckpointView(item))
    }
    return views
}

fun sumEvChargeCheckpointView(view: EvChargeCheckpointView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedQuantity.toDouble() + parsedPallets.toDouble()
}
