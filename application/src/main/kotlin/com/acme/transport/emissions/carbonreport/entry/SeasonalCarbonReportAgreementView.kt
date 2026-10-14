package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal carbon report agreement. */
data class SeasonalCarbonReportAgreementView(
    var code: String,
    var name: String,
    var currency: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toSeasonalCarbonReportAgreementView(item: SeasonalCarbonReportAgreement): SeasonalCarbonReportAgreementView {
    return SeasonalCarbonReportAgreementView(
        code = truncate(item.code, 40),
        name = item.name,
        currency = item.currency,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCarbonReportAgreementViews(items: MutableList<SeasonalCarbonReportAgreement>): MutableList<SeasonalCarbonReportAgreementView> {
    val views = mutableListOf<SeasonalCarbonReportAgreementView>()
    for (item in items) {
        views.add(toSeasonalCarbonReportAgreementView(item))
    }
    return views
}

fun sumSeasonalCarbonReportAgreementView(view: SeasonalCarbonReportAgreementView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
