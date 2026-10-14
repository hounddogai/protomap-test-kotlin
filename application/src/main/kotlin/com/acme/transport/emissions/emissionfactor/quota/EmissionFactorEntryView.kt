package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor entry. */
data class EmissionFactorEntryView(
    var codeText: String,
    var name: String,
    var weight: String,
    var source: String,
    var ceiling: String,
    var updatedAtText: String,
    var country: String,
    var locale: String,
    var statusLabel: String,
)

fun toEmissionFactorEntryView(item: EmissionFactorEntry): EmissionFactorEntryView {
    return EmissionFactorEntryView(
        codeText = item.code,
        name = truncate(item.name, 40),
        weight = "%.2f".format(item.weight),
        source = item.source,
        ceiling = "%.2f".format(item.ceiling),
        updatedAtText = item.updatedAt.toString(),
        country = item.country,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorEntryViews(items: MutableList<EmissionFactorEntry>): MutableList<EmissionFactorEntryView> {
    val views = mutableListOf<EmissionFactorEntryView>()
    for (item in items) {
        views.add(toEmissionFactorEntryView(item))
    }
    return views
}

fun sumEmissionFactorEntryView(view: EmissionFactorEntryView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedWeight + parsedCeiling + parsedUpdatedAt.toDouble()
}
