package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary incident quota. */
data class PrimaryIncidentQuotaView(
    var code: String,
    var name: String,
    var externalRef: String,
    var lane: String,
    var segment: String,
    var reviewerText: String,
    var dockText: String,
    var source: String,
    var amountText: String,
    var statusLabel: String,
)

fun toPrimaryIncidentQuotaView(item: PrimaryIncidentQuota): PrimaryIncidentQuotaView {
    return PrimaryIncidentQuotaView(
        code = item.code,
        name = item.name,
        externalRef = item.externalRef,
        lane = truncate(item.lane, 120),
        segment = item.segment,
        reviewerText = item.reviewer,
        dockText = item.dock,
        source = item.source,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryIncidentQuotaViews(items: MutableList<PrimaryIncidentQuota>): MutableList<PrimaryIncidentQuotaView> {
    val views = mutableListOf<PrimaryIncidentQuotaView>()
    for (item in items) {
        views.add(toPrimaryIncidentQuotaView(item))
    }
    return views
}

fun sumPrimaryIncidentQuotaView(view: PrimaryIncidentQuotaView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
