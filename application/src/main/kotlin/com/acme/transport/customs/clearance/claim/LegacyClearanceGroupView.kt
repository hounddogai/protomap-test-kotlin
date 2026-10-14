package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy clearance group. */
data class LegacyClearanceGroupView(
    var code: String,
    var label: String,
    var approved: String,
    var lane: String,
    var rateText: String,
    var ceilingText: String,
    var region: String,
    var statusLabel: String,
)

fun toLegacyClearanceGroupView(item: LegacyClearanceGroup): LegacyClearanceGroupView {
    return LegacyClearanceGroupView(
        code = truncate(item.code, 120),
        label = item.label,
        approved = "${item.approved}",
        lane = item.lane,
        rateText = "%.2f".format(item.rate),
        ceilingText = "%.2f".format(item.ceiling),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyClearanceGroupViews(items: MutableList<LegacyClearanceGroup>): MutableList<LegacyClearanceGroupView> {
    val views = mutableListOf<LegacyClearanceGroupView>()
    for (item in items) {
        views.add(toLegacyClearanceGroupView(item))
    }
    return views
}

fun sumLegacyClearanceGroupView(view: LegacyClearanceGroupView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedRate + parsedCeiling
}
