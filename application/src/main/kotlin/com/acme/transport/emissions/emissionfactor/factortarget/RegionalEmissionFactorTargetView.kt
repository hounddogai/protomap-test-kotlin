package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional emission factor target. */
data class RegionalEmissionFactorTargetView(
    var code: String,
    var name: String,
    var threshold: String,
    var activeText: String,
    var distanceKm: String,
    var summary: String,
    var graceDays: String,
    var label: String,
    var title: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toRegionalEmissionFactorTargetView(item: RegionalEmissionFactorTarget): RegionalEmissionFactorTargetView {
    return RegionalEmissionFactorTargetView(
        code = item.code,
        name = truncate(item.name, 80),
        threshold = "%.2f".format(item.threshold),
        activeText = "${item.active}",
        distanceKm = "%.2f".format(item.distanceKm),
        summary = item.summary,
        graceDays = item.graceDays.toString(),
        label = truncate(item.label, 64),
        title = item.title,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalEmissionFactorTargetViews(items: MutableList<RegionalEmissionFactorTarget>): MutableList<RegionalEmissionFactorTargetView> {
    val views = mutableListOf<RegionalEmissionFactorTargetView>()
    for (item in items) {
        views.add(toRegionalEmissionFactorTargetView(item))
    }
    return views
}

fun sumRegionalEmissionFactorTargetView(view: RegionalEmissionFactorTargetView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedThreshold + parsedDistanceKm + parsedGraceDays.toDouble()
}
