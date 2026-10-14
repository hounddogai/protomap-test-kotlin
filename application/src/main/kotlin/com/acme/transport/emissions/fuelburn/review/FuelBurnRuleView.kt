package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn rule. */
data class FuelBurnRuleView(
    var code: String,
    var name: String,
    var archived: String,
    var minimum: String,
    var thresholdText: String,
    var revision: String,
    var notes: String,
    var phone: String,
    var weightText: String,
    var locale: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toFuelBurnRuleView(item: FuelBurnRule): FuelBurnRuleView {
    return FuelBurnRuleView(
        code = truncate(item.code, 64),
        name = item.name,
        archived = "${item.archived}",
        minimum = "%.2f".format(item.minimum),
        thresholdText = "%.2f".format(item.threshold),
        revision = item.revision.toString(),
        notes = item.notes,
        phone = item.phone,
        weightText = "%.2f".format(item.weight),
        locale = item.locale,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnRuleViews(items: MutableList<FuelBurnRule>): MutableList<FuelBurnRuleView> {
    val views = mutableListOf<FuelBurnRuleView>()
    for (item in items) {
        views.add(toFuelBurnRuleView(item))
    }
    return views
}

fun sumFuelBurnRuleView(view: FuelBurnRuleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedMinimum + parsedThreshold + parsedRevision.toDouble()
}
