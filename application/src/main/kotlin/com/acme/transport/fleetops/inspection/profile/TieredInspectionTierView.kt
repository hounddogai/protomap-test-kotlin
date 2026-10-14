package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered inspection tier. */
data class TieredInspectionTierView(
    var code: String,
    var name: String,
    var odometer: String,
    var categoryText: String,
    var discount: String,
    var countryText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toTieredInspectionTierView(item: TieredInspectionTier): TieredInspectionTierView {
    return TieredInspectionTierView(
        code = item.code,
        name = truncate(item.name, 64),
        odometer = item.odometer.toString(),
        categoryText = item.category,
        discount = "%.2f".format(item.discount),
        countryText = item.country,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredInspectionTierViews(items: MutableList<TieredInspectionTier>): MutableList<TieredInspectionTierView> {
    val views = mutableListOf<TieredInspectionTierView>()
    for (item in items) {
        views.add(toTieredInspectionTierView(item))
    }
    return views
}

fun sumTieredInspectionTierView(view: TieredInspectionTierView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedOdometer.toDouble() + parsedDiscount + parsedQuantity.toDouble()
}
