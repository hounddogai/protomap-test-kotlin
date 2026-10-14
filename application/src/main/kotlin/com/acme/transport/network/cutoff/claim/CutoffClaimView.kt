package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff claim. */
data class CutoffClaimView(
    var code: String,
    var labelText: String,
    var cost: String,
    var floorValue: String,
    var endsAt: String,
    var categoryText: String,
    var version: String,
    var active: String,
    var statusLabel: String,
)

fun toCutoffClaimView(item: CutoffClaim): CutoffClaimView {
    return CutoffClaimView(
        code = item.code,
        labelText = truncate(item.label, 120),
        cost = "%.2f".format(item.cost),
        floorValue = "%.2f".format(item.floorValue),
        endsAt = item.endsAt.toString(),
        categoryText = truncate(item.category, 80),
        version = item.version.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffClaimViews(items: MutableList<CutoffClaim>): MutableList<CutoffClaimView> {
    val views = mutableListOf<CutoffClaimView>()
    for (item in items) {
        views.add(toCutoffClaimView(item))
    }
    return views
}

fun sumCutoffClaimView(view: CutoffClaimView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedCost + parsedFloorValue + parsedEndsAt.toDouble()
}
