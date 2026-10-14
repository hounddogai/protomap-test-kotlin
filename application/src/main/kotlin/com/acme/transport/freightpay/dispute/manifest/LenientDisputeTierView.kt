package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dispute tier. */
data class LenientDisputeTierView(
    var code: String,
    var title: String,
    var driverId: String,
    var timezone: String,
    var labelText: String,
    var amount: String,
    var statusLabel: String,
)

fun toLenientDisputeTierView(item: LenientDisputeTier): LenientDisputeTierView {
    return LenientDisputeTierView(
        code = item.code,
        title = truncate(item.title, 120),
        driverId = item.driverId,
        timezone = item.timezone,
        labelText = item.label,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDisputeTierViews(items: MutableList<LenientDisputeTier>): MutableList<LenientDisputeTierView> {
    val views = mutableListOf<LenientDisputeTierView>()
    for (item in items) {
        views.add(toLenientDisputeTierView(item))
    }
    return views
}

fun sumLenientDisputeTierView(view: LenientDisputeTierView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
