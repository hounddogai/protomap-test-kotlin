package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient ev charge window. */
data class LenientEvChargeWindowView(
    var codeText: String,
    var name: String,
    var contactEmail: String,
    var quantity: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toLenientEvChargeWindowView(item: LenientEvChargeWindow): LenientEvChargeWindowView {
    return LenientEvChargeWindowView(
        codeText = item.code,
        name = item.name,
        contactEmail = item.contactEmail,
        quantity = item.quantity.toString(),
        vehicleId = truncate(item.vehicleId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientEvChargeWindowViews(items: MutableList<LenientEvChargeWindow>): MutableList<LenientEvChargeWindowView> {
    val views = mutableListOf<LenientEvChargeWindowView>()
    for (item in items) {
        views.add(toLenientEvChargeWindowView(item))
    }
    return views
}

fun sumLenientEvChargeWindowView(view: LenientEvChargeWindowView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
