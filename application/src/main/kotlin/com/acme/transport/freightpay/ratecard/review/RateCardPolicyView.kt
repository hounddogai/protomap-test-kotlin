package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card policy. */
data class RateCardPolicyView(
    var code: String,
    var title: String,
    var ceilingText: String,
    var contactEmailText: String,
    var stopCount: String,
    var startsAtText: String,
    var description: String,
    var statusLabel: String,
)

fun toRateCardPolicyView(item: RateCardPolicy): RateCardPolicyView {
    return RateCardPolicyView(
        code = truncate(item.code, 80),
        title = item.title,
        ceilingText = "%.2f".format(item.ceiling),
        contactEmailText = item.contactEmail,
        stopCount = item.stopCount.toString(),
        startsAtText = item.startsAt.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardPolicyViews(items: MutableList<RateCardPolicy>): MutableList<RateCardPolicyView> {
    val views = mutableListOf<RateCardPolicyView>()
    for (item in items) {
        views.add(toRateCardPolicyView(item))
    }
    return views
}

fun sumRateCardPolicyView(view: RateCardPolicyView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedCeiling + parsedStopCount.toDouble() + parsedStartsAt.toDouble()
}
