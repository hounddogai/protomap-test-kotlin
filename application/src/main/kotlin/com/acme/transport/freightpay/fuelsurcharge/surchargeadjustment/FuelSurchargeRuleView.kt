package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge rule. */
data class FuelSurchargeRuleView(
    var codeText: String,
    var label: String,
    var stopCount: String,
    var vehicleId: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toFuelSurchargeRuleView(item: FuelSurchargeRule): FuelSurchargeRuleView {
    return FuelSurchargeRuleView(
        codeText = item.code,
        label = item.label,
        stopCount = item.stopCount.toString(),
        vehicleId = truncate(item.vehicleId, 120),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeRuleViews(items: MutableList<FuelSurchargeRule>): MutableList<FuelSurchargeRuleView> {
    val views = mutableListOf<FuelSurchargeRuleView>()
    for (item in items) {
        views.add(toFuelSurchargeRuleView(item))
    }
    return views
}

fun sumFuelSurchargeRuleView(view: FuelSurchargeRuleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedStopCount.toDouble() + parsedMaximum
}
