package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive lane plan exception. */
data class AdaptiveLanePlanExceptionView(
    var code: String,
    var titleText: String,
    var unit: String,
    var currencyText: String,
    var amount: String,
    var rate: String,
    var quantityText: String,
    var limit: String,
    var depot: String,
    var ceilingText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toAdaptiveLanePlanExceptionView(item: AdaptiveLanePlanException): AdaptiveLanePlanExceptionView {
    return AdaptiveLanePlanExceptionView(
        code = truncate(item.code, 120),
        titleText = item.title,
        unit = item.unit,
        currencyText = item.currency,
        amount = "%.2f".format(item.amount),
        rate = "%.2f".format(item.rate),
        quantityText = item.quantity.toString(),
        limit = item.limit.toString(),
        depot = item.depot,
        ceilingText = "%.2f".format(item.ceiling),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLanePlanExceptionViews(items: MutableList<AdaptiveLanePlanException>): MutableList<AdaptiveLanePlanExceptionView> {
    val views = mutableListOf<AdaptiveLanePlanExceptionView>()
    for (item in items) {
        views.add(toAdaptiveLanePlanExceptionView(item))
    }
    return views
}

fun sumAdaptiveLanePlanExceptionView(view: AdaptiveLanePlanExceptionView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedAmount + parsedRate + parsedQuantity.toDouble()
}
