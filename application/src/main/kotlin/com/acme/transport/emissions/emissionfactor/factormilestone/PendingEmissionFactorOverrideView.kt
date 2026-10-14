package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending emission factor override. */
data class PendingEmissionFactorOverrideView(
    var code: String,
    var name: String,
    var driverId: String,
    var loadKg: String,
    var windowDays: String,
    var category: String,
    var statusLabel: String,
)

fun toPendingEmissionFactorOverrideView(item: PendingEmissionFactorOverride): PendingEmissionFactorOverrideView {
    return PendingEmissionFactorOverrideView(
        code = item.code,
        name = truncate(item.name, 120),
        driverId = truncate(item.driverId, 64),
        loadKg = "%.2f".format(item.loadKg),
        windowDays = item.windowDays.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingEmissionFactorOverrideViews(items: MutableList<PendingEmissionFactorOverride>): MutableList<PendingEmissionFactorOverrideView> {
    val views = mutableListOf<PendingEmissionFactorOverrideView>()
    for (item in items) {
        views.add(toPendingEmissionFactorOverrideView(item))
    }
    return views
}

fun sumPendingEmissionFactorOverrideView(view: PendingEmissionFactorOverrideView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedLoadKg + parsedWindowDays.toDouble()
}
