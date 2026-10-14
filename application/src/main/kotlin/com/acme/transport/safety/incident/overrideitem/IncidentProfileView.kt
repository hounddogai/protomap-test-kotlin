package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident profile. */
data class IncidentProfileView(
    var codeText: String,
    var label: String,
    var lane: String,
    var thresholdText: String,
    var amount: String,
    var unit: String,
    var statusLabel: String,
)

fun toIncidentProfileView(item: IncidentProfile): IncidentProfileView {
    return IncidentProfileView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 64),
        lane = item.lane,
        thresholdText = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        unit = truncate(item.unit, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentProfileViews(items: MutableList<IncidentProfile>): MutableList<IncidentProfileView> {
    val views = mutableListOf<IncidentProfileView>()
    for (item in items) {
        views.add(toIncidentProfileView(item))
    }
    return views
}

fun sumIncidentProfileView(view: IncidentProfileView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedThreshold + parsedAmount
}
