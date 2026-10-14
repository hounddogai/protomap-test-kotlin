package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional cross dock plan. */
data class RegionalCrossDockPlanView(
    var code: String,
    var titleText: String,
    var timezone: String,
    var quantity: String,
    var locale: String,
    var createdAtText: String,
    var phoneText: String,
    var retries: String,
    var category: String,
    var version: String,
    var statusLabel: String,
)

fun toRegionalCrossDockPlanView(item: RegionalCrossDockPlan): RegionalCrossDockPlanView {
    return RegionalCrossDockPlanView(
        code = truncate(item.code, 80),
        titleText = item.title,
        timezone = truncate(item.timezone, 40),
        quantity = item.quantity.toString(),
        locale = truncate(item.locale, 64),
        createdAtText = item.createdAt.toString(),
        phoneText = item.phone,
        retries = item.retries.toString(),
        category = item.category,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCrossDockPlanViews(items: MutableList<RegionalCrossDockPlan>): MutableList<RegionalCrossDockPlanView> {
    val views = mutableListOf<RegionalCrossDockPlanView>()
    for (item in items) {
        views.add(toRegionalCrossDockPlanView(item))
    }
    return views
}

fun sumRegionalCrossDockPlanView(view: RegionalCrossDockPlanView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedQuantity.toDouble() + parsedCreatedAt.toDouble() + parsedRetries.toDouble()
}
