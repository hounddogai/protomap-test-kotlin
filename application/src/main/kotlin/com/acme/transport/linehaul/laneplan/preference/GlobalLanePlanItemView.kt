package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global lane plan item. */
data class GlobalLanePlanItemView(
    var codeText: String,
    var title: String,
    var rate: String,
    var etaMinutes: String,
    var maximumText: String,
    var reviewer: String,
    var trailerId: String,
    var versionText: String,
    var name: String,
    var statusLabel: String,
)

fun toGlobalLanePlanItemView(item: GlobalLanePlanItem): GlobalLanePlanItemView {
    return GlobalLanePlanItemView(
        codeText = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        etaMinutes = item.etaMinutes.toString(),
        maximumText = "%.2f".format(item.maximum),
        reviewer = item.reviewer,
        trailerId = truncate(item.trailerId, 64),
        versionText = item.version.toString(),
        name = truncate(item.name, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalLanePlanItemViews(items: MutableList<GlobalLanePlanItem>): MutableList<GlobalLanePlanItemView> {
    val views = mutableListOf<GlobalLanePlanItemView>()
    for (item in items) {
        views.add(toGlobalLanePlanItemView(item))
    }
    return views
}

fun sumGlobalLanePlanItemView(view: GlobalLanePlanItemView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedRate + parsedEtaMinutes.toDouble() + parsedMaximum
}
