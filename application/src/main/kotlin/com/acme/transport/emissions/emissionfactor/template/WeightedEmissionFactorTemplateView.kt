package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted emission factor template. */
data class WeightedEmissionFactorTemplateView(
    var codeText: String,
    var name: String,
    var priority: String,
    var contactEmail: String,
    var category: String,
    var visible: String,
    var statusLabel: String,
)

fun toWeightedEmissionFactorTemplateView(item: WeightedEmissionFactorTemplate): WeightedEmissionFactorTemplateView {
    return WeightedEmissionFactorTemplateView(
        codeText = item.code,
        name = truncate(item.name, 40),
        priority = item.priority.toString(),
        contactEmail = item.contactEmail,
        category = item.category,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEmissionFactorTemplateViews(items: MutableList<WeightedEmissionFactorTemplate>): MutableList<WeightedEmissionFactorTemplateView> {
    val views = mutableListOf<WeightedEmissionFactorTemplateView>()
    for (item in items) {
        views.add(toWeightedEmissionFactorTemplateView(item))
    }
    return views
}

fun sumWeightedEmissionFactorTemplateView(view: WeightedEmissionFactorTemplateView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
