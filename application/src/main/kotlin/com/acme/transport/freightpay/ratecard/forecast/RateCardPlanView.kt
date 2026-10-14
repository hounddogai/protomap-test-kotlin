package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card plan. */
data class RateCardPlanView(
    var code: String,
    var titleText: String,
    var unitText: String,
    var cost: String,
    var depotText: String,
    var contactEmail: String,
    var capacity: String,
    var versionText: String,
    var segment: String,
    var channelText: String,
    var statusLabel: String,
)

fun toRateCardPlanView(item: RateCardPlan): RateCardPlanView {
    return RateCardPlanView(
        code = item.code,
        titleText = item.title,
        unitText = item.unit,
        cost = "%.2f".format(item.cost),
        depotText = item.depot,
        contactEmail = item.contactEmail,
        capacity = item.capacity.toString(),
        versionText = item.version.toString(),
        segment = item.segment,
        channelText = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardPlanViews(items: MutableList<RateCardPlan>): MutableList<RateCardPlanView> {
    val views = mutableListOf<RateCardPlanView>()
    for (item in items) {
        views.add(toRateCardPlanView(item))
    }
    return views
}

fun sumRateCardPlanView(view: RateCardPlanView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedCost + parsedCapacity.toDouble() + parsedVersion.toDouble()
}
