package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global trailer yard audit. */
data class GlobalTrailerYardAuditView(
    var code: String,
    var nameText: String,
    var threshold: String,
    var laneText: String,
    var description: String,
    var loadKgText: String,
    var fuelLiters: String,
    var archived: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalTrailerYardAuditView(item: GlobalTrailerYardAudit): GlobalTrailerYardAuditView {
    return GlobalTrailerYardAuditView(
        code = item.code,
        nameText = truncate(item.name, 120),
        threshold = "%.2f".format(item.threshold),
        laneText = truncate(item.lane, 40),
        description = item.description,
        loadKgText = "%.2f".format(item.loadKg),
        fuelLiters = "%.2f".format(item.fuelLiters),
        archived = "${item.archived}",
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTrailerYardAuditViews(items: MutableList<GlobalTrailerYardAudit>): MutableList<GlobalTrailerYardAuditView> {
    val views = mutableListOf<GlobalTrailerYardAuditView>()
    for (item in items) {
        views.add(toGlobalTrailerYardAuditView(item))
    }
    return views
}

fun sumGlobalTrailerYardAuditView(view: GlobalTrailerYardAuditView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedThreshold + parsedLoadKg + parsedFuelLiters
}
