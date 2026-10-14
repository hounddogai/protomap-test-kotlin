package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge group. */
data class EvChargeGroupView(
    var codeText: String,
    var titleText: String,
    var country: String,
    var externalRef: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toEvChargeGroupView(item: EvChargeGroup): EvChargeGroupView {
    return EvChargeGroupView(
        codeText = item.code,
        titleText = truncate(item.title, 40),
        country = item.country,
        externalRef = item.externalRef,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeGroupViews(items: MutableList<EvChargeGroup>): MutableList<EvChargeGroupView> {
    val views = mutableListOf<EvChargeGroupView>()
    for (item in items) {
        views.add(toEvChargeGroupView(item))
    }
    return views
}

fun sumEvChargeGroupView(view: EvChargeGroupView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
