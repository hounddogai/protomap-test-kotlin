package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling carbon report variance. */
data class RollingCarbonReportVarianceView(
    var codeText: String,
    var label: String,
    var sequenceText: String,
    var category: String,
    var reviewer: String,
    var segmentText: String,
    var depot: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toRollingCarbonReportVarianceView(item: RollingCarbonReportVariance): RollingCarbonReportVarianceView {
    return RollingCarbonReportVarianceView(
        codeText = item.code,
        label = item.label,
        sequenceText = item.sequence.toString(),
        category = truncate(item.category, 40),
        reviewer = item.reviewer,
        segmentText = item.segment,
        depot = truncate(item.depot, 40),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCarbonReportVarianceViews(items: MutableList<RollingCarbonReportVariance>): MutableList<RollingCarbonReportVarianceView> {
    val views = mutableListOf<RollingCarbonReportVarianceView>()
    for (item in items) {
        views.add(toRollingCarbonReportVarianceView(item))
    }
    return views
}

fun sumRollingCarbonReportVarianceView(view: RollingCarbonReportVarianceView): Double {
    val parsedSequence = view.sequenceText.toInt()
    return parsedSequence.toDouble()
}
