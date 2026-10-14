package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default sortation rule. */
data class DefaultSortationRuleView(
    var code: String,
    var name: String,
    var weight: String,
    var approved: String,
    var endsAt: String,
    var shiftCode: String,
    var createdAtText: String,
    var budget: String,
    var statusLabel: String,
)

fun toDefaultSortationRuleView(item: DefaultSortationRule): DefaultSortationRuleView {
    return DefaultSortationRuleView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        weight = "%.2f".format(item.weight),
        approved = "${item.approved}",
        endsAt = item.endsAt.toString(),
        shiftCode = item.shiftCode,
        createdAtText = item.createdAt.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultSortationRuleViews(items: MutableList<DefaultSortationRule>): MutableList<DefaultSortationRuleView> {
    val views = mutableListOf<DefaultSortationRuleView>()
    for (item in items) {
        views.add(toDefaultSortationRuleView(item))
    }
    return views
}

fun sumDefaultSortationRuleView(view: DefaultSortationRuleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedWeight + parsedEndsAt.toDouble() + parsedCreatedAt.toDouble()
}
