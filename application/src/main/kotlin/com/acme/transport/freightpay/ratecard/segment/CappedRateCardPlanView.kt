package com.acme.transport.freightpay.ratecard.segment

/** How screens and exports show a capped rate card plan. */
data class CappedRateCardPlanView(
    var code: String,
    var nameText: String,
    var score: String,
    var durationMinutes: String,
    var routeCodeText: String,
    var maximumText: String,
    var locale: String,
    var cost: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toCappedRateCardPlanView(item: CappedRateCardPlan): CappedRateCardPlanView {
    return CappedRateCardPlanView(
        code = item.code,
        nameText = item.name,
        score = "%.2f".format(item.score),
        durationMinutes = item.durationMinutes.toString(),
        routeCodeText = item.routeCode,
        maximumText = "%.2f".format(item.maximum),
        locale = item.locale,
        cost = "%.2f".format(item.cost),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRateCardPlanViews(items: MutableList<CappedRateCardPlan>): MutableList<CappedRateCardPlanView> {
    val views = mutableListOf<CappedRateCardPlanView>()
    for (item in items) {
        views.add(toCappedRateCardPlanView(item))
    }
    return views
}

fun sumCappedRateCardPlanView(view: CappedRateCardPlanView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedScore + parsedDurationMinutes.toDouble() + parsedMaximum
}
