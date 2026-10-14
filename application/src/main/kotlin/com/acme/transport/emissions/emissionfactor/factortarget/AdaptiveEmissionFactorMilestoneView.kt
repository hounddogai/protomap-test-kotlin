package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive emission factor milestone. */
data class AdaptiveEmissionFactorMilestoneView(
    var code: String,
    var title: String,
    var currency: String,
    var version: String,
    var margin: String,
    var statusLabel: String,
)

fun toAdaptiveEmissionFactorMilestoneView(item: AdaptiveEmissionFactorMilestone): AdaptiveEmissionFactorMilestoneView {
    return AdaptiveEmissionFactorMilestoneView(
        code = item.code,
        title = truncate(item.title, 64),
        currency = item.currency,
        version = item.version.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEmissionFactorMilestoneViews(items: MutableList<AdaptiveEmissionFactorMilestone>): MutableList<AdaptiveEmissionFactorMilestoneView> {
    val views = mutableListOf<AdaptiveEmissionFactorMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveEmissionFactorMilestoneView(item))
    }
    return views
}

fun sumAdaptiveEmissionFactorMilestoneView(view: AdaptiveEmissionFactorMilestoneView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedVersion.toDouble() + parsedMargin
}
