package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped hold rollup. */
data class CappedHoldRollupView(
    var code: String,
    var titleText: String,
    var revision: String,
    var cost: String,
    var ratioText: String,
    var updatedAt: String,
    var amount: String,
    var priority: String,
    var carrierText: String,
    var name: String,
    var summary: String,
    var statusLabel: String,
)

fun toCappedHoldRollupView(item: CappedHoldRollup): CappedHoldRollupView {
    return CappedHoldRollupView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 64),
        revision = item.revision.toString(),
        cost = "%.2f".format(item.cost),
        ratioText = "%.2f".format(item.ratio),
        updatedAt = item.updatedAt.toString(),
        amount = "%.2f".format(item.amount),
        priority = item.priority.toString(),
        carrierText = item.carrier,
        name = item.name,
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHoldRollupViews(items: MutableList<CappedHoldRollup>): MutableList<CappedHoldRollupView> {
    val views = mutableListOf<CappedHoldRollupView>()
    for (item in items) {
        views.add(toCappedHoldRollupView(item))
    }
    return views
}

fun sumCappedHoldRollupView(view: CappedHoldRollupView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedRevision.toDouble() + parsedCost + parsedRatio
}
