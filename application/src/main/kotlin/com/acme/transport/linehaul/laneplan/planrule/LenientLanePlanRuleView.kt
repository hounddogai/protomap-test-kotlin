package com.acme.transport.linehaul.laneplan.planrule

/** How screens and exports show a lenient lane plan rule. */
data class LenientLanePlanRuleView(
    var code: String,
    var titleText: String,
    var budget: String,
    var archived: String,
    var revisionText: String,
    var fuelLitersText: String,
    var batchSize: String,
    var capacity: String,
    var amountText: String,
    var statusLabel: String,
)

fun toLenientLanePlanRuleView(item: LenientLanePlanRule): LenientLanePlanRuleView {
    return LenientLanePlanRuleView(
        code = item.code,
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        archived = "${item.archived}",
        revisionText = item.revision.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        batchSize = item.batchSize.toString(),
        capacity = item.capacity.toString(),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientLanePlanRuleViews(items: MutableList<LenientLanePlanRule>): MutableList<LenientLanePlanRuleView> {
    val views = mutableListOf<LenientLanePlanRuleView>()
    for (item in items) {
        views.add(toLenientLanePlanRuleView(item))
    }
    return views
}

fun sumLenientLanePlanRuleView(view: LenientLanePlanRuleView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRevision = view.revisionText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedBudget + parsedRevision.toDouble() + parsedFuelLiters
}
