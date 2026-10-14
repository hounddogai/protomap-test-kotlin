package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped carbon report assignment. */
data class CappedCarbonReportAssignmentView(
    var code: String,
    var label: String,
    var description: String,
    var etaMinutes: String,
    var externalRef: String,
    var contactEmailText: String,
    var sequence: String,
    var category: String,
    var statusLabel: String,
)

fun toCappedCarbonReportAssignmentView(item: CappedCarbonReportAssignment): CappedCarbonReportAssignmentView {
    return CappedCarbonReportAssignmentView(
        code = item.code,
        label = truncate(item.label, 80),
        description = item.description,
        etaMinutes = item.etaMinutes.toString(),
        externalRef = truncate(item.externalRef, 120),
        contactEmailText = truncate(item.contactEmail, 80),
        sequence = item.sequence.toString(),
        category = truncate(item.category, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCarbonReportAssignmentViews(items: MutableList<CappedCarbonReportAssignment>): MutableList<CappedCarbonReportAssignmentView> {
    val views = mutableListOf<CappedCarbonReportAssignmentView>()
    for (item in items) {
        views.add(toCappedCarbonReportAssignmentView(item))
    }
    return views
}

fun sumCappedCarbonReportAssignmentView(view: CappedCarbonReportAssignmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedEtaMinutes.toDouble() + parsedSequence.toDouble()
}
