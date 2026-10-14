package com.acme.transport.freightpay.ratecard.cardbundle

/** How screens and exports show a strict rate card plan. */
data class StrictRateCardPlanView(
    var code: String,
    var title: String,
    var pallets: String,
    var descriptionText: String,
    var limit: String,
    var statusLabel: String,
)

fun toStrictRateCardPlanView(item: StrictRateCardPlan): StrictRateCardPlanView {
    return StrictRateCardPlanView(
        code = item.code,
        title = item.title,
        pallets = item.pallets.toString(),
        descriptionText = item.description,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRateCardPlanViews(items: MutableList<StrictRateCardPlan>): MutableList<StrictRateCardPlanView> {
    val views = mutableListOf<StrictRateCardPlanView>()
    for (item in items) {
        views.add(toStrictRateCardPlanView(item))
    }
    return views
}

fun sumStrictRateCardPlanView(view: StrictRateCardPlanView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedPallets.toDouble() + parsedLimit.toDouble()
}
