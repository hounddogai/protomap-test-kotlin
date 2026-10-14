package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability mapping. */
data class AvailabilityMappingView(
    var codeText: String,
    var name: String,
    var pinnedText: String,
    var currencyText: String,
    var notes: String,
    var quantity: String,
    var statusLabel: String,
)

fun toAvailabilityMappingView(item: AvailabilityMapping): AvailabilityMappingView {
    return AvailabilityMappingView(
        codeText = truncate(item.code, 64),
        name = item.name,
        pinnedText = "${item.pinned}",
        currencyText = item.currency,
        notes = item.notes,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityMappingViews(items: MutableList<AvailabilityMapping>): MutableList<AvailabilityMappingView> {
    val views = mutableListOf<AvailabilityMappingView>()
    for (item in items) {
        views.add(toAvailabilityMappingView(item))
    }
    return views
}

fun sumAvailabilityMappingView(view: AvailabilityMappingView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
