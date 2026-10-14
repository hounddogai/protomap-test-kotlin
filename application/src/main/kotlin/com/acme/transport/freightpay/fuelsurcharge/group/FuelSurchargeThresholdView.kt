package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge threshold. */
data class FuelSurchargeThresholdView(
    var code: String,
    var title: String,
    var contactEmail: String,
    var currency: String,
    var priority: String,
    var statusLabel: String,
)

fun toFuelSurchargeThresholdView(item: FuelSurchargeThreshold): FuelSurchargeThresholdView {
    return FuelSurchargeThresholdView(
        code = item.code,
        title = item.title,
        contactEmail = truncate(item.contactEmail, 80),
        currency = item.currency,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeThresholdViews(items: MutableList<FuelSurchargeThreshold>): MutableList<FuelSurchargeThresholdView> {
    val views = mutableListOf<FuelSurchargeThresholdView>()
    for (item in items) {
        views.add(toFuelSurchargeThresholdView(item))
    }
    return views
}

fun sumFuelSurchargeThresholdView(view: FuelSurchargeThresholdView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
