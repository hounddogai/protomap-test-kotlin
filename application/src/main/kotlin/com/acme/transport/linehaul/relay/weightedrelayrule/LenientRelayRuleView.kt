package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient relay rule. */
data class LenientRelayRuleView(
    var codeText: String,
    var name: String,
    var odometer: String,
    var marginText: String,
    var ceiling: String,
    var timezoneText: String,
    var country: String,
    var durationMinutes: String,
    var unit: String,
    var statusLabel: String,
)

fun toLenientRelayRuleView(item: LenientRelayRule): LenientRelayRuleView {
    return LenientRelayRuleView(
        codeText = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        marginText = "%.2f".format(item.margin),
        ceiling = "%.2f".format(item.ceiling),
        timezoneText = item.timezone,
        country = truncate(item.country, 64),
        durationMinutes = item.durationMinutes.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRelayRuleViews(items: MutableList<LenientRelayRule>): MutableList<LenientRelayRuleView> {
    val views = mutableListOf<LenientRelayRuleView>()
    for (item in items) {
        views.add(toLenientRelayRuleView(item))
    }
    return views
}

fun sumLenientRelayRuleView(view: LenientRelayRuleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedOdometer.toDouble() + parsedMargin + parsedCeiling
}
