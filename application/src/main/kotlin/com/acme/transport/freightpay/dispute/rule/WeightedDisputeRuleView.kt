package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dispute rule. */
data class WeightedDisputeRuleView(
    var code: String,
    var labelText: String,
    var notes: String,
    var minimumText: String,
    var updatedAt: String,
    var limit: String,
    var revision: String,
    var trailerId: String,
    var archived: String,
    var statusLabel: String,
)

fun toWeightedDisputeRuleView(item: WeightedDisputeRule): WeightedDisputeRuleView {
    return WeightedDisputeRuleView(
        code = truncate(item.code, 40),
        labelText = item.label,
        notes = item.notes,
        minimumText = "%.2f".format(item.minimum),
        updatedAt = item.updatedAt.toString(),
        limit = item.limit.toString(),
        revision = item.revision.toString(),
        trailerId = item.trailerId,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDisputeRuleViews(items: MutableList<WeightedDisputeRule>): MutableList<WeightedDisputeRuleView> {
    val views = mutableListOf<WeightedDisputeRuleView>()
    for (item in items) {
        views.add(toWeightedDisputeRuleView(item))
    }
    return views
}

fun sumWeightedDisputeRuleView(view: WeightedDisputeRuleView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedMinimum + parsedUpdatedAt.toDouble() + parsedLimit.toDouble()
}
