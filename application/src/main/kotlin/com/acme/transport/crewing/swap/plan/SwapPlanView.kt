package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap plan. */
data class SwapPlanView(
    var code: String,
    var titleText: String,
    var margin: String,
    var phone: String,
    var contactEmailText: String,
    var notes: String,
    var laneText: String,
    var region: String,
    var currency: String,
    var quantity: String,
    var statusLabel: String,
)

fun toSwapPlanView(item: SwapPlan): SwapPlanView {
    return SwapPlanView(
        code = truncate(item.code, 120),
        titleText = item.title,
        margin = "%.2f".format(item.margin),
        phone = item.phone,
        contactEmailText = item.contactEmail,
        notes = item.notes,
        laneText = item.lane,
        region = item.region,
        currency = truncate(item.currency, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapPlanViews(items: MutableList<SwapPlan>): MutableList<SwapPlanView> {
    val views = mutableListOf<SwapPlanView>()
    for (item in items) {
        views.add(toSwapPlanView(item))
    }
    return views
}

fun sumSwapPlanView(view: SwapPlanView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedMargin + parsedQuantity.toDouble()
}
