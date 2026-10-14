package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending fuel surcharge variance. */
data class PendingFuelSurchargeVarianceView(
    var codeText: String,
    var titleText: String,
    var phone: String,
    var floorValue: String,
    var budget: String,
    var externalRef: String,
    var name: String,
    var statusLabel: String,
)

fun toPendingFuelSurchargeVarianceView(item: PendingFuelSurchargeVariance): PendingFuelSurchargeVarianceView {
    return PendingFuelSurchargeVarianceView(
        codeText = item.code,
        titleText = item.title,
        phone = truncate(item.phone, 64),
        floorValue = "%.2f".format(item.floorValue),
        budget = "%.2f".format(item.budget),
        externalRef = item.externalRef,
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFuelSurchargeVarianceViews(items: MutableList<PendingFuelSurchargeVariance>): MutableList<PendingFuelSurchargeVarianceView> {
    val views = mutableListOf<PendingFuelSurchargeVarianceView>()
    for (item in items) {
        views.add(toPendingFuelSurchargeVarianceView(item))
    }
    return views
}

fun sumPendingFuelSurchargeVarianceView(view: PendingFuelSurchargeVarianceView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedFloorValue + parsedBudget
}
