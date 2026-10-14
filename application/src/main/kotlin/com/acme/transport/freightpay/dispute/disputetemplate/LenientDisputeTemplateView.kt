package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dispute template. */
data class LenientDisputeTemplateView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var amount: String,
    var dock: String,
    var statusLabel: String,
)

fun toLenientDisputeTemplateView(item: LenientDisputeTemplate): LenientDisputeTemplateView {
    return LenientDisputeTemplateView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 80),
        expiresAt = item.expiresAt.toString(),
        amount = "%.2f".format(item.amount),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDisputeTemplateViews(items: MutableList<LenientDisputeTemplate>): MutableList<LenientDisputeTemplateView> {
    val views = mutableListOf<LenientDisputeTemplateView>()
    for (item in items) {
        views.add(toLenientDisputeTemplateView(item))
    }
    return views
}

fun sumLenientDisputeTemplateView(view: LenientDisputeTemplateView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedExpiresAt.toDouble() + parsedAmount
}
