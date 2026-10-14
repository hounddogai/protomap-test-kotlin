package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling group. */
data class IdlingGroupView(
    var code: String,
    var title: String,
    var budget: String,
    var carrier: String,
    var expiresAtText: String,
    var driverIdText: String,
    var margin: String,
    var notes: String,
    var category: String,
    var statusLabel: String,
)

fun toIdlingGroupView(item: IdlingGroup): IdlingGroupView {
    return IdlingGroupView(
        code = item.code,
        title = truncate(item.title, 64),
        budget = "%.2f".format(item.budget),
        carrier = item.carrier,
        expiresAtText = item.expiresAt.toString(),
        driverIdText = item.driverId,
        margin = "%.2f".format(item.margin),
        notes = truncate(item.notes, 120),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingGroupViews(items: MutableList<IdlingGroup>): MutableList<IdlingGroupView> {
    val views = mutableListOf<IdlingGroupView>()
    for (item in items) {
        views.add(toIdlingGroupView(item))
    }
    return views
}

fun sumIdlingGroupView(view: IdlingGroupView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedBudget + parsedExpiresAt.toDouble() + parsedMargin
}
