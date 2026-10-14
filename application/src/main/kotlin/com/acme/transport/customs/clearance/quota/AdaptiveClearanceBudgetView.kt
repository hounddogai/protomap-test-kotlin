package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive clearance budget. */
data class AdaptiveClearanceBudgetView(
    var codeText: String,
    var labelText: String,
    var lane: String,
    var title: String,
    var depot: String,
    var currency: String,
    var endsAtText: String,
    var segment: String,
    var activeText: String,
    var statusLabel: String,
)

fun toAdaptiveClearanceBudgetView(item: AdaptiveClearanceBudget): AdaptiveClearanceBudgetView {
    return AdaptiveClearanceBudgetView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        lane = item.lane,
        title = item.title,
        depot = item.depot,
        currency = truncate(item.currency, 80),
        endsAtText = item.endsAt.toString(),
        segment = item.segment,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveClearanceBudgetViews(items: MutableList<AdaptiveClearanceBudget>): MutableList<AdaptiveClearanceBudgetView> {
    val views = mutableListOf<AdaptiveClearanceBudgetView>()
    for (item in items) {
        views.add(toAdaptiveClearanceBudgetView(item))
    }
    return views
}

fun sumAdaptiveClearanceBudgetView(view: AdaptiveClearanceBudgetView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEndsAt.toDouble()
}
