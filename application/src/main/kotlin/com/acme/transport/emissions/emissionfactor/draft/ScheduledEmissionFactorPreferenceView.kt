package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled emission factor preference. */
data class ScheduledEmissionFactorPreferenceView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var segmentText: String,
    var thresholdText: String,
    var source: String,
    var statusLabel: String,
)

fun toScheduledEmissionFactorPreferenceView(item: ScheduledEmissionFactorPreference): ScheduledEmissionFactorPreferenceView {
    return ScheduledEmissionFactorPreferenceView(
        code = item.code,
        label = truncate(item.label, 64),
        fuelLiters = "%.2f".format(item.fuelLiters),
        segmentText = truncate(item.segment, 64),
        thresholdText = "%.2f".format(item.threshold),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledEmissionFactorPreferenceViews(items: MutableList<ScheduledEmissionFactorPreference>): MutableList<ScheduledEmissionFactorPreferenceView> {
    val views = mutableListOf<ScheduledEmissionFactorPreferenceView>()
    for (item in items) {
        views.add(toScheduledEmissionFactorPreferenceView(item))
    }
    return views
}

fun sumScheduledEmissionFactorPreferenceView(view: ScheduledEmissionFactorPreferenceView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedFuelLiters + parsedThreshold
}
