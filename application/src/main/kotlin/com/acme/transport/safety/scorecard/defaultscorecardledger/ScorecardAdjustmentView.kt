package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard adjustment. */
data class ScorecardAdjustmentView(
    var code: String,
    var title: String,
    var approvedText: String,
    var trailerId: String,
    var discount: String,
    var statusLabel: String,
)

fun toScorecardAdjustmentView(item: ScorecardAdjustment): ScorecardAdjustmentView {
    return ScorecardAdjustmentView(
        code = item.code,
        title = item.title,
        approvedText = "${item.approved}",
        trailerId = truncate(item.trailerId, 64),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardAdjustmentViews(items: MutableList<ScorecardAdjustment>): MutableList<ScorecardAdjustmentView> {
    val views = mutableListOf<ScorecardAdjustmentView>()
    for (item in items) {
        views.add(toScorecardAdjustmentView(item))
    }
    return views
}

fun sumScorecardAdjustmentView(view: ScorecardAdjustmentView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
