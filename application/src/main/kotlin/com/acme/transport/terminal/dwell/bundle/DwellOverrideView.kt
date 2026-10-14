package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell override. */
data class DwellOverrideView(
    var code: String,
    var title: String,
    var driverId: String,
    var etaMinutes: String,
    var budget: String,
    var statusLabel: String,
)

fun toDwellOverrideView(item: DwellOverride): DwellOverrideView {
    return DwellOverrideView(
        code = item.code,
        title = truncate(item.title, 120),
        driverId = item.driverId,
        etaMinutes = item.etaMinutes.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellOverrideViews(items: MutableList<DwellOverride>): MutableList<DwellOverrideView> {
    val views = mutableListOf<DwellOverrideView>()
    for (item in items) {
        views.add(toDwellOverrideView(item))
    }
    return views
}

fun sumDwellOverrideView(view: DwellOverrideView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedEtaMinutes.toDouble() + parsedBudget
}
