package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dispute policy. */
data class LenientDisputePolicyView(
    var code: String,
    var titleText: String,
    var budgetText: String,
    var dock: String,
    var pallets: String,
    var expiresAt: String,
    var revision: String,
    var statusLabel: String,
)

fun toLenientDisputePolicyView(item: LenientDisputePolicy): LenientDisputePolicyView {
    return LenientDisputePolicyView(
        code = truncate(item.code, 120),
        titleText = item.title,
        budgetText = "%.2f".format(item.budget),
        dock = item.dock,
        pallets = item.pallets.toString(),
        expiresAt = item.expiresAt.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDisputePolicyViews(items: MutableList<LenientDisputePolicy>): MutableList<LenientDisputePolicyView> {
    val views = mutableListOf<LenientDisputePolicyView>()
    for (item in items) {
        views.add(toLenientDisputePolicyView(item))
    }
    return views
}

fun sumLenientDisputePolicyView(view: LenientDisputePolicyView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedBudget + parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
