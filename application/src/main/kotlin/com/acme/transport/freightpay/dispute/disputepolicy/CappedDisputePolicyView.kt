package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dispute policy. */
data class CappedDisputePolicyView(
    var codeText: String,
    var title: String,
    var graceDays: String,
    var limit: String,
    var locale: String,
    var ownerId: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toCappedDisputePolicyView(item: CappedDisputePolicy): CappedDisputePolicyView {
    return CappedDisputePolicyView(
        codeText = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        limit = item.limit.toString(),
        locale = item.locale,
        ownerId = truncate(item.ownerId, 80),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDisputePolicyViews(items: MutableList<CappedDisputePolicy>): MutableList<CappedDisputePolicyView> {
    val views = mutableListOf<CappedDisputePolicyView>()
    for (item in items) {
        views.add(toCappedDisputePolicyView(item))
    }
    return views
}

fun sumCappedDisputePolicyView(view: CappedDisputePolicyView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedGraceDays.toDouble() + parsedLimit.toDouble()
}
