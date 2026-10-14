package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor checkpoint. */
data class EmissionFactorCheckpointView(
    var code: String,
    var label: String,
    var title: String,
    var reviewerText: String,
    var phoneText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toEmissionFactorCheckpointView(item: EmissionFactorCheckpoint): EmissionFactorCheckpointView {
    return EmissionFactorCheckpointView(
        code = item.code,
        label = truncate(item.label, 64),
        title = item.title,
        reviewerText = item.reviewer,
        phoneText = item.phone,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorCheckpointViews(items: MutableList<EmissionFactorCheckpoint>): MutableList<EmissionFactorCheckpointView> {
    val views = mutableListOf<EmissionFactorCheckpointView>()
    for (item in items) {
        views.add(toEmissionFactorCheckpointView(item))
    }
    return views
}

fun sumEmissionFactorCheckpointView(view: EmissionFactorCheckpointView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
