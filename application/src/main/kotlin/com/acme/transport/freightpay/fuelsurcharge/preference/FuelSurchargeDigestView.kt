package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge digest. */
data class FuelSurchargeDigestView(
    var code: String,
    var title: String,
    var discount: String,
    var rate: String,
    var graceDaysText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toFuelSurchargeDigestView(item: FuelSurchargeDigest): FuelSurchargeDigestView {
    return FuelSurchargeDigestView(
        code = item.code,
        title = truncate(item.title, 40),
        discount = "%.2f".format(item.discount),
        rate = "%.2f".format(item.rate),
        graceDaysText = item.graceDays.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeDigestViews(items: MutableList<FuelSurchargeDigest>): MutableList<FuelSurchargeDigestView> {
    val views = mutableListOf<FuelSurchargeDigestView>()
    for (item in items) {
        views.add(toFuelSurchargeDigestView(item))
    }
    return views
}

fun sumFuelSurchargeDigestView(view: FuelSurchargeDigestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedDiscount + parsedRate + parsedGraceDays.toDouble()
}
