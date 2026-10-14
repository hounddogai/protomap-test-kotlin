package com.acme.transport.linehaul.laneplan.review

/** How screens and exports show a adaptive lane plan rule. */
data class AdaptiveLanePlanRuleView(
    var code: String,
    var title: String,
    var weight: String,
    var sequence: String,
    var pallets: String,
    var archived: String,
    var category: String,
    var statusLabel: String,
)

fun toAdaptiveLanePlanRuleView(item: AdaptiveLanePlanRule): AdaptiveLanePlanRuleView {
    return AdaptiveLanePlanRuleView(
        code = item.code,
        title = item.title,
        weight = "%.2f".format(item.weight),
        sequence = item.sequence.toString(),
        pallets = item.pallets.toString(),
        archived = "${item.archived}",
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLanePlanRuleViews(items: MutableList<AdaptiveLanePlanRule>): MutableList<AdaptiveLanePlanRuleView> {
    val views = mutableListOf<AdaptiveLanePlanRuleView>()
    for (item in items) {
        views.add(toAdaptiveLanePlanRuleView(item))
    }
    return views
}

fun sumAdaptiveLanePlanRuleView(view: AdaptiveLanePlanRuleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedWeight + parsedSequence.toDouble() + parsedPallets.toDouble()
}
