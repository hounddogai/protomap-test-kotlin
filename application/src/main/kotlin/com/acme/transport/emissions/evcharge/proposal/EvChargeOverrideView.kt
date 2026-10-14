package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge override. */
data class EvChargeOverrideView(
    var code: String,
    var labelText: String,
    var archived: String,
    var durationMinutesText: String,
    var endsAt: String,
    var budgetText: String,
    var region: String,
    var countryText: String,
    var statusLabel: String,
)

fun toEvChargeOverrideView(item: EvChargeOverride): EvChargeOverrideView {
    return EvChargeOverrideView(
        code = item.code,
        labelText = item.label,
        archived = "${item.archived}",
        durationMinutesText = item.durationMinutes.toString(),
        endsAt = item.endsAt.toString(),
        budgetText = "%.2f".format(item.budget),
        region = truncate(item.region, 80),
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeOverrideViews(items: MutableList<EvChargeOverride>): MutableList<EvChargeOverrideView> {
    val views = mutableListOf<EvChargeOverrideView>()
    for (item in items) {
        views.add(toEvChargeOverrideView(item))
    }
    return views
}

fun sumEvChargeOverrideView(view: EvChargeOverrideView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble() + parsedBudget
}
