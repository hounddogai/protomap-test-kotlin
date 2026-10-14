package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional fuel surcharge group. */
data class RegionalFuelSurchargeGroupView(
    var code: String,
    var label: String,
    var routeCode: String,
    var contactEmail: String,
    var channel: String,
    var scoreText: String,
    var externalRefText: String,
    var reviewer: String,
    var sourceText: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toRegionalFuelSurchargeGroupView(item: RegionalFuelSurchargeGroup): RegionalFuelSurchargeGroupView {
    return RegionalFuelSurchargeGroupView(
        code = item.code,
        label = item.label,
        routeCode = truncate(item.routeCode, 40),
        contactEmail = truncate(item.contactEmail, 120),
        channel = item.channel,
        scoreText = "%.2f".format(item.score),
        externalRefText = item.externalRef,
        reviewer = item.reviewer,
        sourceText = item.source,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelSurchargeGroupViews(items: MutableList<RegionalFuelSurchargeGroup>): MutableList<RegionalFuelSurchargeGroupView> {
    val views = mutableListOf<RegionalFuelSurchargeGroupView>()
    for (item in items) {
        views.add(toRegionalFuelSurchargeGroupView(item))
    }
    return views
}

fun sumRegionalFuelSurchargeGroupView(view: RegionalFuelSurchargeGroupView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedScore + parsedPriority.toDouble()
}
