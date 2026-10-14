package com.acme.transport.emissions.carbonreport.reportagreement

/** How screens and exports show a fallback carbon report agreement. */
data class FallbackCarbonReportAgreementView(
    var code: String,
    var title: String,
    var budget: String,
    var ratio: String,
    var rate: String,
    var expiresAt: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toFallbackCarbonReportAgreementView(item: FallbackCarbonReportAgreement): FallbackCarbonReportAgreementView {
    return FallbackCarbonReportAgreementView(
        code = item.code,
        title = item.title,
        budget = "%.2f".format(item.budget),
        ratio = "%.2f".format(item.ratio),
        rate = "%.2f".format(item.rate),
        expiresAt = item.expiresAt.toString(),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCarbonReportAgreementViews(items: MutableList<FallbackCarbonReportAgreement>): MutableList<FallbackCarbonReportAgreementView> {
    val views = mutableListOf<FallbackCarbonReportAgreementView>()
    for (item in items) {
        views.add(toFallbackCarbonReportAgreementView(item))
    }
    return views
}

fun sumFallbackCarbonReportAgreementView(view: FallbackCarbonReportAgreementView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedBudget + parsedRatio + parsedRate
}
