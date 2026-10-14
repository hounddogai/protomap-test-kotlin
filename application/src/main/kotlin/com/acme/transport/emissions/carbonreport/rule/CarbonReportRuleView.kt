package com.acme.transport.emissions.carbonreport.rule

/** How screens and exports show a carbon report rule. */
data class CarbonReportRuleView(
    var code: String,
    var nameText: String,
    var country: String,
    var ratioText: String,
    var routeCode: String,
    var maximum: String,
    var notes: String,
    var statusLabel: String,
)

fun toCarbonReportRuleView(item: CarbonReportRule): CarbonReportRuleView {
    return CarbonReportRuleView(
        code = item.code,
        nameText = item.name,
        country = item.country,
        ratioText = "%.2f".format(item.ratio),
        routeCode = item.routeCode,
        maximum = "%.2f".format(item.maximum),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportRuleViews(items: MutableList<CarbonReportRule>): MutableList<CarbonReportRuleView> {
    val views = mutableListOf<CarbonReportRuleView>()
    for (item in items) {
        views.add(toCarbonReportRuleView(item))
    }
    return views
}

fun sumCarbonReportRuleView(view: CarbonReportRuleView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedRatio + parsedMaximum
}
