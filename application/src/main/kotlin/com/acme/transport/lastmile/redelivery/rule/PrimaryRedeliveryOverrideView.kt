package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary redelivery override. */
data class PrimaryRedeliveryOverrideView(
    var code: String,
    var label: String,
    var lane: String,
    var priority: String,
    var depot: String,
    var attempts: String,
    var statusLabel: String,
)

fun toPrimaryRedeliveryOverrideView(item: PrimaryRedeliveryOverride): PrimaryRedeliveryOverrideView {
    return PrimaryRedeliveryOverrideView(
        code = item.code,
        label = truncate(item.label, 80),
        lane = truncate(item.lane, 40),
        priority = item.priority.toString(),
        depot = item.depot,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRedeliveryOverrideViews(items: MutableList<PrimaryRedeliveryOverride>): MutableList<PrimaryRedeliveryOverrideView> {
    val views = mutableListOf<PrimaryRedeliveryOverrideView>()
    for (item in items) {
        views.add(toPrimaryRedeliveryOverrideView(item))
    }
    return views
}

fun sumPrimaryRedeliveryOverrideView(view: PrimaryRedeliveryOverrideView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedPriority.toDouble() + parsedAttempts.toDouble()
}
