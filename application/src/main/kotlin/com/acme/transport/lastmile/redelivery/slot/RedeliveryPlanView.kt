package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery plan. */
data class RedeliveryPlanView(
    var code: String,
    var label: String,
    var summary: String,
    var carrier: String,
    var marginText: String,
    var statusLabel: String,
)

fun toRedeliveryPlanView(item: RedeliveryPlan): RedeliveryPlanView {
    return RedeliveryPlanView(
        code = item.code,
        label = item.label,
        summary = truncate(item.summary, 64),
        carrier = item.carrier,
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryPlanViews(items: MutableList<RedeliveryPlan>): MutableList<RedeliveryPlanView> {
    val views = mutableListOf<RedeliveryPlanView>()
    for (item in items) {
        views.add(toRedeliveryPlanView(item))
    }
    return views
}

fun sumRedeliveryPlanView(view: RedeliveryPlanView): Double {
    val parsedMargin = view.marginText.toDouble()
    return parsedMargin
}
