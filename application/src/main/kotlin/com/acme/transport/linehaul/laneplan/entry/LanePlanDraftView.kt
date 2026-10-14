package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan draft. */
data class LanePlanDraftView(
    var codeText: String,
    var name: String,
    var unitText: String,
    var timezoneText: String,
    var category: String,
    var rate: String,
    var region: String,
    var palletsText: String,
    var etaMinutes: String,
    var dockText: String,
    var statusLabel: String,
)

fun toLanePlanDraftView(item: LanePlanDraft): LanePlanDraftView {
    return LanePlanDraftView(
        codeText = item.code,
        name = truncate(item.name, 120),
        unitText = truncate(item.unit, 64),
        timezoneText = item.timezone,
        category = item.category,
        rate = "%.2f".format(item.rate),
        region = item.region,
        palletsText = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanDraftViews(items: MutableList<LanePlanDraft>): MutableList<LanePlanDraftView> {
    val views = mutableListOf<LanePlanDraftView>()
    for (item in items) {
        views.add(toLanePlanDraftView(item))
    }
    return views
}

fun sumLanePlanDraftView(view: LanePlanDraftView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRate + parsedPallets.toDouble() + parsedEtaMinutes.toDouble()
}
