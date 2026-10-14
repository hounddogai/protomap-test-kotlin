package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor preference. */
data class EmissionFactorPreferenceView(
    var code: String,
    var label: String,
    var sequence: String,
    var limitText: String,
    var region: String,
    var distanceKm: String,
    var updatedAt: String,
    var summary: String,
    var titleText: String,
    var statusLabel: String,
)

fun toEmissionFactorPreferenceView(item: EmissionFactorPreference): EmissionFactorPreferenceView {
    return EmissionFactorPreferenceView(
        code = item.code,
        label = item.label,
        sequence = item.sequence.toString(),
        limitText = item.limit.toString(),
        region = truncate(item.region, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        updatedAt = item.updatedAt.toString(),
        summary = truncate(item.summary, 80),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorPreferenceViews(items: MutableList<EmissionFactorPreference>): MutableList<EmissionFactorPreferenceView> {
    val views = mutableListOf<EmissionFactorPreferenceView>()
    for (item in items) {
        views.add(toEmissionFactorPreferenceView(item))
    }
    return views
}

fun sumEmissionFactorPreferenceView(view: EmissionFactorPreferenceView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedSequence.toDouble() + parsedLimit.toDouble() + parsedDistanceKm
}
