package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict roster exception. */
data class StrictRosterExceptionView(
    var code: String,
    var label: String,
    var cost: String,
    var fuelLiters: String,
    var retries: String,
    var score: String,
    var budgetText: String,
    var dock: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toStrictRosterExceptionView(item: StrictRosterException): StrictRosterExceptionView {
    return StrictRosterExceptionView(
        code = truncate(item.code, 64),
        label = item.label,
        cost = "%.2f".format(item.cost),
        fuelLiters = "%.2f".format(item.fuelLiters),
        retries = item.retries.toString(),
        score = "%.2f".format(item.score),
        budgetText = "%.2f".format(item.budget),
        dock = item.dock,
        contactEmail = truncate(item.contactEmail, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRosterExceptionViews(items: MutableList<StrictRosterException>): MutableList<StrictRosterExceptionView> {
    val views = mutableListOf<StrictRosterExceptionView>()
    for (item in items) {
        views.add(toStrictRosterExceptionView(item))
    }
    return views
}

fun sumStrictRosterExceptionView(view: StrictRosterExceptionView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedCost + parsedFuelLiters + parsedRetries.toDouble()
}
