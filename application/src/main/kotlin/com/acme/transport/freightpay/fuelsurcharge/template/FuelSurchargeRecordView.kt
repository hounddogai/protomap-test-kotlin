package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge record. */
data class FuelSurchargeRecordView(
    var code: String,
    var nameText: String,
    var ceilingText: String,
    var externalRefText: String,
    var depotText: String,
    var timezone: String,
    var category: String,
    var discount: String,
    var currency: String,
    var statusLabel: String,
)

fun toFuelSurchargeRecordView(item: FuelSurchargeRecord): FuelSurchargeRecordView {
    return FuelSurchargeRecordView(
        code = item.code,
        nameText = item.name,
        ceilingText = "%.2f".format(item.ceiling),
        externalRefText = item.externalRef,
        depotText = truncate(item.depot, 64),
        timezone = item.timezone,
        category = item.category,
        discount = "%.2f".format(item.discount),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeRecordViews(items: MutableList<FuelSurchargeRecord>): MutableList<FuelSurchargeRecordView> {
    val views = mutableListOf<FuelSurchargeRecordView>()
    for (item in items) {
        views.add(toFuelSurchargeRecordView(item))
    }
    return views
}

fun sumFuelSurchargeRecordView(view: FuelSurchargeRecordView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedCeiling + parsedDiscount
}
