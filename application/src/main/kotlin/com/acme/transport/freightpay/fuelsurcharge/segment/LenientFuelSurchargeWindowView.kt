package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient fuel surcharge window. */
data class LenientFuelSurchargeWindowView(
    var code: String,
    var nameText: String,
    var limit: String,
    var unitText: String,
    var summary: String,
    var score: String,
    var amountText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toLenientFuelSurchargeWindowView(item: LenientFuelSurchargeWindow): LenientFuelSurchargeWindowView {
    return LenientFuelSurchargeWindowView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 120),
        limit = item.limit.toString(),
        unitText = truncate(item.unit, 120),
        summary = truncate(item.summary, 64),
        score = "%.2f".format(item.score),
        amountText = "%.2f".format(item.amount),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelSurchargeWindowViews(items: MutableList<LenientFuelSurchargeWindow>): MutableList<LenientFuelSurchargeWindowView> {
    val views = mutableListOf<LenientFuelSurchargeWindowView>()
    for (item in items) {
        views.add(toLenientFuelSurchargeWindowView(item))
    }
    return views
}

fun sumLenientFuelSurchargeWindowView(view: LenientFuelSurchargeWindowView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedScore = view.score.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedLimit.toDouble() + parsedScore + parsedAmount
}
