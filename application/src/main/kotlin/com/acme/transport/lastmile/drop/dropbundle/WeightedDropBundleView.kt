package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted drop bundle. */
data class WeightedDropBundleView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var phoneText: String,
    var unit: String,
    var statusLabel: String,
)

fun toWeightedDropBundleView(item: WeightedDropBundle): WeightedDropBundleView {
    return WeightedDropBundleView(
        code = truncate(item.code, 40),
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        phoneText = item.phone,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDropBundleViews(items: MutableList<WeightedDropBundle>): MutableList<WeightedDropBundleView> {
    val views = mutableListOf<WeightedDropBundleView>()
    for (item in items) {
        views.add(toWeightedDropBundleView(item))
    }
    return views
}

fun sumWeightedDropBundleView(view: WeightedDropBundleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
