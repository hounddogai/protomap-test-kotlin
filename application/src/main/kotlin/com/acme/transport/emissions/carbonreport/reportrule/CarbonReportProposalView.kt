package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report proposal. */
data class CarbonReportProposalView(
    var code: String,
    var title: String,
    var label: String,
    var checksum: String,
    var thresholdText: String,
    var etaMinutes: String,
    var source: String,
    var statusLabel: String,
)

fun toCarbonReportProposalView(item: CarbonReportProposal): CarbonReportProposalView {
    return CarbonReportProposalView(
        code = item.code,
        title = item.title,
        label = truncate(item.label, 40),
        checksum = item.checksum,
        thresholdText = "%.2f".format(item.threshold),
        etaMinutes = item.etaMinutes.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportProposalViews(items: MutableList<CarbonReportProposal>): MutableList<CarbonReportProposalView> {
    val views = mutableListOf<CarbonReportProposalView>()
    for (item in items) {
        views.add(toCarbonReportProposalView(item))
    }
    return views
}

fun sumCarbonReportProposalView(view: CarbonReportProposalView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedThreshold + parsedEtaMinutes.toDouble()
}
