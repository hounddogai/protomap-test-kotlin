package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tender plan. */
data class FallbackTenderPlanView(
    var code: String,
    var name: String,
    var stopCount: String,
    var startsAt: String,
    var capacity: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toFallbackTenderPlanView(item: FallbackTenderPlan): FallbackTenderPlanView {
    return FallbackTenderPlanView(
        code = truncate(item.code, 80),
        name = item.name,
        stopCount = item.stopCount.toString(),
        startsAt = item.startsAt.toString(),
        capacity = item.capacity.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTenderPlanViews(items: MutableList<FallbackTenderPlan>): MutableList<FallbackTenderPlanView> {
    val views = mutableListOf<FallbackTenderPlanView>()
    for (item in items) {
        views.add(toFallbackTenderPlanView(item))
    }
    return views
}

fun sumFallbackTenderPlanView(view: FallbackTenderPlanView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedStopCount.toDouble() + parsedStartsAt.toDouble() + parsedCapacity.toDouble()
}
