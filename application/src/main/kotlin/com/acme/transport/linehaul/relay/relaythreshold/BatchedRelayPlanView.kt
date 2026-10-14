package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched relay plan. */
data class BatchedRelayPlanView(
    var code: String,
    var label: String,
    var reviewer: String,
    var shiftCode: String,
    var carrier: String,
    var visible: String,
    var startsAt: String,
    var trailerId: String,
    var limitText: String,
    var durationMinutes: String,
    var timezone: String,
    var statusLabel: String,
)

fun toBatchedRelayPlanView(item: BatchedRelayPlan): BatchedRelayPlanView {
    return BatchedRelayPlanView(
        code = item.code,
        label = item.label,
        reviewer = item.reviewer,
        shiftCode = truncate(item.shiftCode, 80),
        carrier = item.carrier,
        visible = "${item.visible}",
        startsAt = item.startsAt.toString(),
        trailerId = truncate(item.trailerId, 80),
        limitText = item.limit.toString(),
        durationMinutes = item.durationMinutes.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRelayPlanViews(items: MutableList<BatchedRelayPlan>): MutableList<BatchedRelayPlanView> {
    val views = mutableListOf<BatchedRelayPlanView>()
    for (item in items) {
        views.add(toBatchedRelayPlanView(item))
    }
    return views
}

fun sumBatchedRelayPlanView(view: BatchedRelayPlanView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedStartsAt.toDouble() + parsedLimit.toDouble() + parsedDurationMinutes.toDouble()
}
