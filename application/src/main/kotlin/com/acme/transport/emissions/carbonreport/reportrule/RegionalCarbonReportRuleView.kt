package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional carbon report rule. */
data class RegionalCarbonReportRuleView(
    var codeText: String,
    var label: String,
    var contactEmail: String,
    var unitText: String,
    var reviewer: String,
    var timezone: String,
    var active: String,
    var revisionText: String,
    var driverIdText: String,
    var regionText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportRuleView(item: RegionalCarbonReportRule): RegionalCarbonReportRuleView {
    return RegionalCarbonReportRuleView(
        codeText = item.code,
        label = truncate(item.label, 40),
        contactEmail = truncate(item.contactEmail, 120),
        unitText = item.unit,
        reviewer = item.reviewer,
        timezone = truncate(item.timezone, 64),
        active = "${item.active}",
        revisionText = item.revision.toString(),
        driverIdText = item.driverId,
        regionText = truncate(item.region, 64),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportRuleViews(items: MutableList<RegionalCarbonReportRule>): MutableList<RegionalCarbonReportRuleView> {
    val views = mutableListOf<RegionalCarbonReportRuleView>()
    for (item in items) {
        views.add(toRegionalCarbonReportRuleView(item))
    }
    return views
}

fun sumRegionalCarbonReportRuleView(view: RegionalCarbonReportRuleView): Double {
    val parsedRevision = view.revisionText.toInt()
    return parsedRevision.toDouble()
}
