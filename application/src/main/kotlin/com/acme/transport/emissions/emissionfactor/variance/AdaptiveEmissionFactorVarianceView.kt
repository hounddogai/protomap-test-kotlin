package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive emission factor variance. */
data class AdaptiveEmissionFactorVarianceView(
    var code: String,
    var label: String,
    var amount: String,
    var localeText: String,
    var statusLabel: String,
)

fun toAdaptiveEmissionFactorVarianceView(item: AdaptiveEmissionFactorVariance): AdaptiveEmissionFactorVarianceView {
    return AdaptiveEmissionFactorVarianceView(
        code = item.code,
        label = truncate(item.label, 40),
        amount = "%.2f".format(item.amount),
        localeText = truncate(item.locale, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEmissionFactorVarianceViews(items: MutableList<AdaptiveEmissionFactorVariance>): MutableList<AdaptiveEmissionFactorVarianceView> {
    val views = mutableListOf<AdaptiveEmissionFactorVarianceView>()
    for (item in items) {
        views.add(toAdaptiveEmissionFactorVarianceView(item))
    }
    return views
}

fun sumAdaptiveEmissionFactorVarianceView(view: AdaptiveEmissionFactorVarianceView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
