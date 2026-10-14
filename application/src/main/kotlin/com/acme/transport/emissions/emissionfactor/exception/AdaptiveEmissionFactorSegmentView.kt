package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive emission factor segment. */
data class AdaptiveEmissionFactorSegmentView(
    var code: String,
    var label: String,
    var maximum: String,
    var unit: String,
    var carrierText: String,
    var revision: String,
    var statusLabel: String,
)

fun toAdaptiveEmissionFactorSegmentView(item: AdaptiveEmissionFactorSegment): AdaptiveEmissionFactorSegmentView {
    return AdaptiveEmissionFactorSegmentView(
        code = truncate(item.code, 80),
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        unit = item.unit,
        carrierText = item.carrier,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEmissionFactorSegmentViews(items: MutableList<AdaptiveEmissionFactorSegment>): MutableList<AdaptiveEmissionFactorSegmentView> {
    val views = mutableListOf<AdaptiveEmissionFactorSegmentView>()
    for (item in items) {
        views.add(toAdaptiveEmissionFactorSegmentView(item))
    }
    return views
}

fun sumAdaptiveEmissionFactorSegmentView(view: AdaptiveEmissionFactorSegmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedMaximum + parsedRevision.toDouble()
}
