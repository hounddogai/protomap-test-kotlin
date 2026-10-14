package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default coaching budget. */
data class DefaultCoachingBudgetView(
    var code: String,
    var label: String,
    var channel: String,
    var rate: String,
    var pinned: String,
    var source: String,
    var statusLabel: String,
)

fun toDefaultCoachingBudgetView(item: DefaultCoachingBudget): DefaultCoachingBudgetView {
    return DefaultCoachingBudgetView(
        code = truncate(item.code, 40),
        label = item.label,
        channel = item.channel,
        rate = "%.2f".format(item.rate),
        pinned = "${item.pinned}",
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCoachingBudgetViews(items: MutableList<DefaultCoachingBudget>): MutableList<DefaultCoachingBudgetView> {
    val views = mutableListOf<DefaultCoachingBudgetView>()
    for (item in items) {
        views.add(toDefaultCoachingBudgetView(item))
    }
    return views
}

fun sumDefaultCoachingBudgetView(view: DefaultCoachingBudgetView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
