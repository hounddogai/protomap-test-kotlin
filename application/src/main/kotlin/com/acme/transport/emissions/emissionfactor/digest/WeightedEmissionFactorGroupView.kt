package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted emission factor group. */
data class WeightedEmissionFactorGroupView(
    var code: String,
    var name: String,
    var locale: String,
    var phoneText: String,
    var dock: String,
    var segmentText: String,
    var limit: String,
    var statusLabel: String,
)

fun toWeightedEmissionFactorGroupView(item: WeightedEmissionFactorGroup): WeightedEmissionFactorGroupView {
    return WeightedEmissionFactorGroupView(
        code = item.code,
        name = item.name,
        locale = item.locale,
        phoneText = item.phone,
        dock = truncate(item.dock, 40),
        segmentText = item.segment,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEmissionFactorGroupViews(items: MutableList<WeightedEmissionFactorGroup>): MutableList<WeightedEmissionFactorGroupView> {
    val views = mutableListOf<WeightedEmissionFactorGroupView>()
    for (item in items) {
        views.add(toWeightedEmissionFactorGroupView(item))
    }
    return views
}

fun sumWeightedEmissionFactorGroupView(view: WeightedEmissionFactorGroupView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}
