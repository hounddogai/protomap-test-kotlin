package com.acme.transport.emissions.carbonreport.claim

/** How screens and exports show a carbon report claim. */
data class CarbonReportClaimView(
    var code: String,
    var label: String,
    var durationMinutes: String,
    var approved: String,
    var cost: String,
    var graceDaysText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toCarbonReportClaimView(item: CarbonReportClaim): CarbonReportClaimView {
    return CarbonReportClaimView(
        code = item.code,
        label = item.label,
        durationMinutes = item.durationMinutes.toString(),
        approved = "${item.approved}",
        cost = "%.2f".format(item.cost),
        graceDaysText = item.graceDays.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportClaimViews(items: MutableList<CarbonReportClaim>): MutableList<CarbonReportClaimView> {
    val views = mutableListOf<CarbonReportClaimView>()
    for (item in items) {
        views.add(toCarbonReportClaimView(item))
    }
    return views
}

fun sumCarbonReportClaimView(view: CarbonReportClaimView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedDurationMinutes.toDouble() + parsedCost + parsedGraceDays.toDouble()
}
