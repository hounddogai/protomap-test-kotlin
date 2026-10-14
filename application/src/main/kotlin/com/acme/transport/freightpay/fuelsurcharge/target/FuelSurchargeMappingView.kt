package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge mapping. */
data class FuelSurchargeMappingView(
    var code: String,
    var labelText: String,
    var phoneText: String,
    var externalRef: String,
    var country: String,
    var locale: String,
    var rate: String,
    var statusLabel: String,
)

fun toFuelSurchargeMappingView(item: FuelSurchargeMapping): FuelSurchargeMappingView {
    return FuelSurchargeMappingView(
        code = truncate(item.code, 64),
        labelText = item.label,
        phoneText = item.phone,
        externalRef = item.externalRef,
        country = truncate(item.country, 120),
        locale = item.locale,
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeMappingViews(items: MutableList<FuelSurchargeMapping>): MutableList<FuelSurchargeMappingView> {
    val views = mutableListOf<FuelSurchargeMappingView>()
    for (item in items) {
        views.add(toFuelSurchargeMappingView(item))
    }
    return views
}

fun sumFuelSurchargeMappingView(view: FuelSurchargeMappingView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
