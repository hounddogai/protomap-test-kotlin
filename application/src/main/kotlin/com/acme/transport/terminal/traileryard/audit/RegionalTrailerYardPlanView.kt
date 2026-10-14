package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional trailer yard plan. */
data class RegionalTrailerYardPlanView(
    var codeText: String,
    var title: String,
    var locale: String,
    var pinnedText: String,
    var etaMinutesText: String,
    var routeCode: String,
    var maximum: String,
    var statusLabel: String,
)

fun toRegionalTrailerYardPlanView(item: RegionalTrailerYardPlan): RegionalTrailerYardPlanView {
    return RegionalTrailerYardPlanView(
        codeText = item.code,
        title = truncate(item.title, 64),
        locale = item.locale,
        pinnedText = "${item.pinned}",
        etaMinutesText = item.etaMinutes.toString(),
        routeCode = item.routeCode,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTrailerYardPlanViews(items: MutableList<RegionalTrailerYardPlan>): MutableList<RegionalTrailerYardPlanView> {
    val views = mutableListOf<RegionalTrailerYardPlanView>()
    for (item in items) {
        views.add(toRegionalTrailerYardPlanView(item))
    }
    return views
}

fun sumRegionalTrailerYardPlanView(view: RegionalTrailerYardPlanView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedEtaMinutes.toDouble() + parsedMaximum
}
