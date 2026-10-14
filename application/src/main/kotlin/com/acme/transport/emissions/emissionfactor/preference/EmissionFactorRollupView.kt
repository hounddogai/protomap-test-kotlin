package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor rollup. */
data class EmissionFactorRollupView(
    var code: String,
    var titleText: String,
    var threshold: String,
    var ratioText: String,
    var rate: String,
    var shiftCode: String,
    var unit: String,
    var cost: String,
    var durationMinutes: String,
    var version: String,
    var statusLabel: String,
)

fun toEmissionFactorRollupView(item: EmissionFactorRollup): EmissionFactorRollupView {
    return EmissionFactorRollupView(
        code = truncate(item.code, 120),
        titleText = item.title,
        threshold = "%.2f".format(item.threshold),
        ratioText = "%.2f".format(item.ratio),
        rate = "%.2f".format(item.rate),
        shiftCode = item.shiftCode,
        unit = item.unit,
        cost = "%.2f".format(item.cost),
        durationMinutes = item.durationMinutes.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorRollupViews(items: MutableList<EmissionFactorRollup>): MutableList<EmissionFactorRollupView> {
    val views = mutableListOf<EmissionFactorRollupView>()
    for (item in items) {
        views.add(toEmissionFactorRollupView(item))
    }
    return views
}

fun sumEmissionFactorRollupView(view: EmissionFactorRollupView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedThreshold + parsedRatio + parsedRate
}
