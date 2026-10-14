package com.acme.transport.safety.incident.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped incident target. */
data class CappedIncidentTargetView(
    var code: String,
    var title: String,
    var rate: String,
    var depotText: String,
    var attempts: String,
    var activeText: String,
    var loadKgText: String,
    var unit: String,
    var quantity: String,
    var statusLabel: String,
)

fun toCappedIncidentTargetView(item: CappedIncidentTarget): CappedIncidentTargetView {
    return CappedIncidentTargetView(
        code = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        depotText = item.depot,
        attempts = item.attempts.toString(),
        activeText = "${item.active}",
        loadKgText = "%.2f".format(item.loadKg),
        unit = truncate(item.unit, 80),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedIncidentTargetViews(items: MutableList<CappedIncidentTarget>): MutableList<CappedIncidentTargetView> {
    val views = mutableListOf<CappedIncidentTargetView>()
    for (item in items) {
        views.add(toCappedIncidentTargetView(item))
    }
    return views
}

fun sumCappedIncidentTargetView(view: CappedIncidentTargetView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedRate + parsedAttempts.toDouble() + parsedLoadKg
}
