package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback carbon report preference. */
data class FallbackCarbonReportPreferenceView(
    var code: String,
    var name: String,
    var active: String,
    var contactEmail: String,
    var currency: String,
    var localeText: String,
    var revision: String,
    var sequenceText: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toFallbackCarbonReportPreferenceView(item: FallbackCarbonReportPreference): FallbackCarbonReportPreferenceView {
    return FallbackCarbonReportPreferenceView(
        code = item.code,
        name = truncate(item.name, 80),
        active = "${item.active}",
        contactEmail = item.contactEmail,
        currency = item.currency,
        localeText = item.locale,
        revision = item.revision.toString(),
        sequenceText = item.sequence.toString(),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCarbonReportPreferenceViews(items: MutableList<FallbackCarbonReportPreference>): MutableList<FallbackCarbonReportPreferenceView> {
    val views = mutableListOf<FallbackCarbonReportPreferenceView>()
    for (item in items) {
        views.add(toFallbackCarbonReportPreferenceView(item))
    }
    return views
}

fun sumFallbackCarbonReportPreferenceView(view: FallbackCarbonReportPreferenceView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedRevision.toDouble() + parsedSequence.toDouble() + parsedOdometer.toDouble()
}
