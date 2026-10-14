package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped drop preference. */
data class CappedDropPreferenceView(
    var code: String,
    var label: String,
    var depotText: String,
    var quantityText: String,
    var phone: String,
    var vehicleId: String,
    var pallets: String,
    var statusLabel: String,
)

fun toCappedDropPreferenceView(item: CappedDropPreference): CappedDropPreferenceView {
    return CappedDropPreferenceView(
        code = item.code,
        label = truncate(item.label, 64),
        depotText = item.depot,
        quantityText = item.quantity.toString(),
        phone = item.phone,
        vehicleId = item.vehicleId,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDropPreferenceViews(items: MutableList<CappedDropPreference>): MutableList<CappedDropPreferenceView> {
    val views = mutableListOf<CappedDropPreferenceView>()
    for (item in items) {
        views.add(toCappedDropPreferenceView(item))
    }
    return views
}

fun sumCappedDropPreferenceView(view: CappedDropPreferenceView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedQuantity.toDouble() + parsedPallets.toDouble()
}
