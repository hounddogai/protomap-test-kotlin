package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary fuel surcharge draft. */
data class PrimaryFuelSurchargeDraftView(
    var codeText: String,
    var nameText: String,
    var shiftCode: String,
    var currency: String,
    var carrier: String,
    var amount: String,
    var floorValue: String,
    var threshold: String,
    var statusLabel: String,
)

fun toPrimaryFuelSurchargeDraftView(item: PrimaryFuelSurchargeDraft): PrimaryFuelSurchargeDraftView {
    return PrimaryFuelSurchargeDraftView(
        codeText = item.code,
        nameText = item.name,
        shiftCode = item.shiftCode,
        currency = truncate(item.currency, 80),
        carrier = item.carrier,
        amount = "%.2f".format(item.amount),
        floorValue = "%.2f".format(item.floorValue),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelSurchargeDraftViews(items: MutableList<PrimaryFuelSurchargeDraft>): MutableList<PrimaryFuelSurchargeDraftView> {
    val views = mutableListOf<PrimaryFuelSurchargeDraftView>()
    for (item in items) {
        views.add(toPrimaryFuelSurchargeDraftView(item))
    }
    return views
}

fun sumPrimaryFuelSurchargeDraftView(view: PrimaryFuelSurchargeDraftView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedAmount + parsedFloorValue + parsedThreshold
}
