package com.acme.transport.freightpay.ratecard.cardrequest

/** How screens and exports show a adaptive rate card plan. */
data class AdaptiveRateCardPlanView(
    var code: String,
    var name: String,
    var scoreText: String,
    var retriesText: String,
    var titleText: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toAdaptiveRateCardPlanView(item: AdaptiveRateCardPlan): AdaptiveRateCardPlanView {
    return AdaptiveRateCardPlanView(
        code = item.code,
        name = item.name,
        scoreText = "%.2f".format(item.score),
        retriesText = item.retries.toString(),
        titleText = item.title,
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRateCardPlanViews(items: MutableList<AdaptiveRateCardPlan>): MutableList<AdaptiveRateCardPlanView> {
    val views = mutableListOf<AdaptiveRateCardPlanView>()
    for (item in items) {
        views.add(toAdaptiveRateCardPlanView(item))
    }
    return views
}

fun sumAdaptiveRateCardPlanView(view: AdaptiveRateCardPlanView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedScore + parsedRetries.toDouble() + parsedPallets.toDouble()
}
