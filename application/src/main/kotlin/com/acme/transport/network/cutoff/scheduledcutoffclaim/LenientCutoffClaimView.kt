package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient cutoff claim. */
data class LenientCutoffClaimView(
    var code: String,
    var name: String,
    var cost: String,
    var createdAtText: String,
    var title: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toLenientCutoffClaimView(item: LenientCutoffClaim): LenientCutoffClaimView {
    return LenientCutoffClaimView(
        code = item.code,
        name = item.name,
        cost = "%.2f".format(item.cost),
        createdAtText = item.createdAt.toString(),
        title = truncate(item.title, 40),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCutoffClaimViews(items: MutableList<LenientCutoffClaim>): MutableList<LenientCutoffClaimView> {
    val views = mutableListOf<LenientCutoffClaimView>()
    for (item in items) {
        views.add(toLenientCutoffClaimView(item))
    }
    return views
}

fun sumLenientCutoffClaimView(view: LenientCutoffClaimView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCost + parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble()
}
