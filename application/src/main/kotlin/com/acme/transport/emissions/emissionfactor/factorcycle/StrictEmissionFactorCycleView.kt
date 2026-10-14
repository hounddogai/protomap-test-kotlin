package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict emission factor cycle. */
data class StrictEmissionFactorCycleView(
    var code: String,
    var title: String,
    var visible: String,
    var contactEmail: String,
    var depotText: String,
    var amount: String,
    var statusLabel: String,
)

fun toStrictEmissionFactorCycleView(item: StrictEmissionFactorCycle): StrictEmissionFactorCycleView {
    return StrictEmissionFactorCycleView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 120),
        visible = "${item.visible}",
        contactEmail = truncate(item.contactEmail, 80),
        depotText = item.depot,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEmissionFactorCycleViews(items: MutableList<StrictEmissionFactorCycle>): MutableList<StrictEmissionFactorCycleView> {
    val views = mutableListOf<StrictEmissionFactorCycleView>()
    for (item in items) {
        views.add(toStrictEmissionFactorCycleView(item))
    }
    return views
}

fun sumStrictEmissionFactorCycleView(view: StrictEmissionFactorCycleView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
