package com.acme.transport.linehaul.laneplan.segment

/** How screens and exports show a lenient lane plan estimate. */
data class LenientLanePlanEstimateView(
    var code: String,
    var title: String,
    var durationMinutesText: String,
    var budget: String,
    var rate: String,
    var statusLabel: String,
)

fun toLenientLanePlanEstimateView(item: LenientLanePlanEstimate): LenientLanePlanEstimateView {
    return LenientLanePlanEstimateView(
        code = item.code,
        title = item.title,
        durationMinutesText = item.durationMinutes.toString(),
        budget = "%.2f".format(item.budget),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientLanePlanEstimateViews(items: MutableList<LenientLanePlanEstimate>): MutableList<LenientLanePlanEstimateView> {
    val views = mutableListOf<LenientLanePlanEstimateView>()
    for (item in items) {
        views.add(toLenientLanePlanEstimateView(item))
    }
    return views
}

fun sumLenientLanePlanEstimateView(view: LenientLanePlanEstimateView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedDurationMinutes.toDouble() + parsedBudget + parsedRate
}
