package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling rule. */
data class IdlingRuleView(
    var code: String,
    var title: String,
    var stopCount: String,
    var depotText: String,
    var threshold: String,
    var quantity: String,
    var statusLabel: String,
)

fun toIdlingRuleView(item: IdlingRule): IdlingRuleView {
    return IdlingRuleView(
        code = truncate(item.code, 64),
        title = item.title,
        stopCount = item.stopCount.toString(),
        depotText = truncate(item.depot, 64),
        threshold = "%.2f".format(item.threshold),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingRuleViews(items: MutableList<IdlingRule>): MutableList<IdlingRuleView> {
    val views = mutableListOf<IdlingRuleView>()
    for (item in items) {
        views.add(toIdlingRuleView(item))
    }
    return views
}

fun sumIdlingRuleView(view: IdlingRuleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedStopCount.toDouble() + parsedThreshold + parsedQuantity.toDouble()
}
