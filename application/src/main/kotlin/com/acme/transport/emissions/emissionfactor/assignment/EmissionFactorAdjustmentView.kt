package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor adjustment. */
data class EmissionFactorAdjustmentView(
    var code: String,
    var labelText: String,
    var summary: String,
    var margin: String,
    var externalRefText: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toEmissionFactorAdjustmentView(item: EmissionFactorAdjustment): EmissionFactorAdjustmentView {
    return EmissionFactorAdjustmentView(
        code = item.code,
        labelText = truncate(item.label, 40),
        summary = item.summary,
        margin = "%.2f".format(item.margin),
        externalRefText = item.externalRef,
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorAdjustmentViews(items: MutableList<EmissionFactorAdjustment>): MutableList<EmissionFactorAdjustmentView> {
    val views = mutableListOf<EmissionFactorAdjustmentView>()
    for (item in items) {
        views.add(toEmissionFactorAdjustmentView(item))
    }
    return views
}

fun sumEmissionFactorAdjustmentView(view: EmissionFactorAdjustmentView): Double {
    val parsedMargin = view.margin.toDouble()
    return parsedMargin
}
