package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling cutoff preference. */
data class RollingCutoffPreferenceView(
    var code: String,
    var name: String,
    var startsAt: String,
    var label: String,
    var capacityText: String,
    var shiftCode: String,
    var cost: String,
    var statusLabel: String,
)

fun toRollingCutoffPreferenceView(item: RollingCutoffPreference): RollingCutoffPreferenceView {
    return RollingCutoffPreferenceView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 64),
        startsAt = item.startsAt.toString(),
        label = item.label,
        capacityText = item.capacity.toString(),
        shiftCode = item.shiftCode,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCutoffPreferenceViews(items: MutableList<RollingCutoffPreference>): MutableList<RollingCutoffPreferenceView> {
    val views = mutableListOf<RollingCutoffPreferenceView>()
    for (item in items) {
        views.add(toRollingCutoffPreferenceView(item))
    }
    return views
}

fun sumRollingCutoffPreferenceView(view: RollingCutoffPreferenceView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedCapacity.toDouble() + parsedCost
}
