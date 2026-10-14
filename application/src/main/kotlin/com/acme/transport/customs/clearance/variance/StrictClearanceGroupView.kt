package com.acme.transport.customs.clearance.variance

/** How screens and exports show a strict clearance group. */
data class StrictClearanceGroupView(
    var code: String,
    var label: String,
    var carrierText: String,
    var etaMinutes: String,
    var name: String,
    var statusLabel: String,
)

fun toStrictClearanceGroupView(item: StrictClearanceGroup): StrictClearanceGroupView {
    return StrictClearanceGroupView(
        code = item.code,
        label = item.label,
        carrierText = item.carrier,
        etaMinutes = item.etaMinutes.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictClearanceGroupViews(items: MutableList<StrictClearanceGroup>): MutableList<StrictClearanceGroupView> {
    val views = mutableListOf<StrictClearanceGroupView>()
    for (item in items) {
        views.add(toStrictClearanceGroupView(item))
    }
    return views
}

fun sumStrictClearanceGroupView(view: StrictClearanceGroupView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
