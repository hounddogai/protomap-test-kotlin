package com.acme.transport.linehaul.tender.tenderbatch

/** How screens and exports show a tender rule. */
data class TenderRuleView(
    var code: String,
    var labelText: String,
    var reviewer: String,
    var fuelLiters: String,
    var active: String,
    var statusLabel: String,
)

fun toTenderRuleView(item: TenderRule): TenderRuleView {
    return TenderRuleView(
        code = item.code,
        labelText = item.label,
        reviewer = item.reviewer,
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderRuleViews(items: MutableList<TenderRule>): MutableList<TenderRuleView> {
    val views = mutableListOf<TenderRuleView>()
    for (item in items) {
        views.add(toTenderRuleView(item))
    }
    return views
}

fun sumTenderRuleView(view: TenderRuleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
