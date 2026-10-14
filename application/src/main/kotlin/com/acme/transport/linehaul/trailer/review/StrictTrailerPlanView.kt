package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict trailer plan. */
data class StrictTrailerPlanView(
    var code: String,
    var label: String,
    var limitText: String,
    var segment: String,
    var startsAtText: String,
    var driverId: String,
    var budget: String,
    var checksum: String,
    var capacity: String,
    var locale: String,
    var statusLabel: String,
)

fun toStrictTrailerPlanView(item: StrictTrailerPlan): StrictTrailerPlanView {
    return StrictTrailerPlanView(
        code = item.code,
        label = item.label,
        limitText = item.limit.toString(),
        segment = item.segment,
        startsAtText = item.startsAt.toString(),
        driverId = truncate(item.driverId, 40),
        budget = "%.2f".format(item.budget),
        checksum = item.checksum,
        capacity = item.capacity.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTrailerPlanViews(items: MutableList<StrictTrailerPlan>): MutableList<StrictTrailerPlanView> {
    val views = mutableListOf<StrictTrailerPlanView>()
    for (item in items) {
        views.add(toStrictTrailerPlanView(item))
    }
    return views
}

fun sumStrictTrailerPlanView(view: StrictTrailerPlanView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedLimit.toDouble() + parsedStartsAt.toDouble() + parsedBudget
}
