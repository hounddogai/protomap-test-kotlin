package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor bundle. */
data class EmissionFactorBundleView(
    var code: String,
    var titleText: String,
    var labelText: String,
    var name: String,
    var pinned: String,
    var marginText: String,
    var quantity: String,
    var source: String,
    var statusLabel: String,
)

fun toEmissionFactorBundleView(item: EmissionFactorBundle): EmissionFactorBundleView {
    return EmissionFactorBundleView(
        code = truncate(item.code, 40),
        titleText = item.title,
        labelText = truncate(item.label, 64),
        name = item.name,
        pinned = "${item.pinned}",
        marginText = "%.2f".format(item.margin),
        quantity = item.quantity.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorBundleViews(items: MutableList<EmissionFactorBundle>): MutableList<EmissionFactorBundleView> {
    val views = mutableListOf<EmissionFactorBundleView>()
    for (item in items) {
        views.add(toEmissionFactorBundleView(item))
    }
    return views
}

fun sumEmissionFactorBundleView(view: EmissionFactorBundleView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedMargin + parsedQuantity.toDouble()
}
