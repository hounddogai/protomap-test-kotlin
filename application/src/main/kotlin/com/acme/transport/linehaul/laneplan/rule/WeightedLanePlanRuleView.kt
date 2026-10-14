package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted lane plan rule. */
data class WeightedLanePlanRuleView(
    var code: String,
    var nameText: String,
    var visibleText: String,
    var priority: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toWeightedLanePlanRuleView(item: WeightedLanePlanRule): WeightedLanePlanRuleView {
    return WeightedLanePlanRuleView(
        code = item.code,
        nameText = item.name,
        visibleText = "${item.visible}",
        priority = item.priority.toString(),
        shiftCode = truncate(item.shiftCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLanePlanRuleViews(items: MutableList<WeightedLanePlanRule>): MutableList<WeightedLanePlanRuleView> {
    val views = mutableListOf<WeightedLanePlanRuleView>()
    for (item in items) {
        views.add(toWeightedLanePlanRuleView(item))
    }
    return views
}

fun sumWeightedLanePlanRuleView(view: WeightedLanePlanRuleView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
