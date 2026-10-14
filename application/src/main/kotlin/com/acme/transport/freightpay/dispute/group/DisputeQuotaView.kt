package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute quota. */
data class DisputeQuotaView(
    var codeText: String,
    var titleText: String,
    var etaMinutes: String,
    var pinnedText: String,
    var timezoneText: String,
    var rate: String,
    var depotText: String,
    var statusLabel: String,
)

fun toDisputeQuotaView(item: DisputeQuota): DisputeQuotaView {
    return DisputeQuotaView(
        codeText = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        pinnedText = "${item.pinned}",
        timezoneText = item.timezone,
        rate = "%.2f".format(item.rate),
        depotText = truncate(item.depot, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeQuotaViews(items: MutableList<DisputeQuota>): MutableList<DisputeQuotaView> {
    val views = mutableListOf<DisputeQuotaView>()
    for (item in items) {
        views.add(toDisputeQuotaView(item))
    }
    return views
}

fun sumDisputeQuotaView(view: DisputeQuotaView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRate
}
