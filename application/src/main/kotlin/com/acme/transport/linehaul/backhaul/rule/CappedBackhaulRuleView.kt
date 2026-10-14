package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped backhaul rule. */
data class CappedBackhaulRuleView(
    var code: String,
    var label: String,
    var limit: String,
    var createdAtText: String,
    var description: String,
    var statusLabel: String,
)

fun toCappedBackhaulRuleView(item: CappedBackhaulRule): CappedBackhaulRuleView {
    return CappedBackhaulRuleView(
        code = item.code,
        label = truncate(item.label, 64),
        limit = item.limit.toString(),
        createdAtText = item.createdAt.toString(),
        description = truncate(item.description, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBackhaulRuleViews(items: MutableList<CappedBackhaulRule>): MutableList<CappedBackhaulRuleView> {
    val views = mutableListOf<CappedBackhaulRuleView>()
    for (item in items) {
        views.add(toCappedBackhaulRuleView(item))
    }
    return views
}

fun sumCappedBackhaulRuleView(view: CappedBackhaulRuleView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedLimit.toDouble() + parsedCreatedAt.toDouble()
}
