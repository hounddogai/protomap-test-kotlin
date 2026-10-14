package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional ev charge template. */
data class RegionalEvChargeTemplateView(
    var code: String,
    var labelText: String,
    var trailerIdText: String,
    var rate: String,
    var revision: String,
    var visible: String,
    var cost: String,
    var reviewer: String,
    var timezoneText: String,
    var active: String,
    var statusLabel: String,
)

fun toRegionalEvChargeTemplateView(item: RegionalEvChargeTemplate): RegionalEvChargeTemplateView {
    return RegionalEvChargeTemplateView(
        code = item.code,
        labelText = truncate(item.label, 64),
        trailerIdText = item.trailerId,
        rate = "%.2f".format(item.rate),
        revision = item.revision.toString(),
        visible = "${item.visible}",
        cost = "%.2f".format(item.cost),
        reviewer = truncate(item.reviewer, 120),
        timezoneText = item.timezone,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalEvChargeTemplateViews(items: MutableList<RegionalEvChargeTemplate>): MutableList<RegionalEvChargeTemplateView> {
    val views = mutableListOf<RegionalEvChargeTemplateView>()
    for (item in items) {
        views.add(toRegionalEvChargeTemplateView(item))
    }
    return views
}

fun sumRegionalEvChargeTemplateView(view: RegionalEvChargeTemplateView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedRate + parsedRevision.toDouble() + parsedCost
}
