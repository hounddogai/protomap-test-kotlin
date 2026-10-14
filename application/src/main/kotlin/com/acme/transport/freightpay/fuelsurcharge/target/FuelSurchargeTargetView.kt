package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge target. */
data class FuelSurchargeTargetView(
    var codeText: String,
    var nameText: String,
    var amount: String,
    var titleText: String,
    var floorValue: String,
    var contactEmail: String,
    var description: String,
    var externalRef: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toFuelSurchargeTargetView(item: FuelSurchargeTarget): FuelSurchargeTargetView {
    return FuelSurchargeTargetView(
        codeText = truncate(item.code, 40),
        nameText = truncate(item.name, 80),
        amount = "%.2f".format(item.amount),
        titleText = truncate(item.title, 120),
        floorValue = "%.2f".format(item.floorValue),
        contactEmail = item.contactEmail,
        description = item.description,
        externalRef = item.externalRef,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeTargetViews(items: MutableList<FuelSurchargeTarget>): MutableList<FuelSurchargeTargetView> {
    val views = mutableListOf<FuelSurchargeTargetView>()
    for (item in items) {
        views.add(toFuelSurchargeTargetView(item))
    }
    return views
}

fun sumFuelSurchargeTargetView(view: FuelSurchargeTargetView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedAmount + parsedFloorValue
}
