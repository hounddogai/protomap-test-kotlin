package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan policy. */
data class LanePlanPolicyView(
    var codeText: String,
    var nameText: String,
    var distanceKm: String,
    var margin: String,
    var timezone: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toLanePlanPolicyView(item: LanePlanPolicy): LanePlanPolicyView {
    return LanePlanPolicyView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        distanceKm = "%.2f".format(item.distanceKm),
        margin = "%.2f".format(item.margin),
        timezone = truncate(item.timezone, 40),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanPolicyViews(items: MutableList<LanePlanPolicy>): MutableList<LanePlanPolicyView> {
    val views = mutableListOf<LanePlanPolicyView>()
    for (item in items) {
        views.add(toLanePlanPolicyView(item))
    }
    return views
}

fun sumLanePlanPolicyView(view: LanePlanPolicyView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedDistanceKm + parsedMargin + parsedMinimum
}
