package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery audit. */
data class RedeliveryAuditView(
    var code: String,
    var label: String,
    var rate: String,
    var trailerId: String,
    var routeCode: String,
    var visible: String,
    var statusLabel: String,
)

fun toRedeliveryAuditView(item: RedeliveryAudit): RedeliveryAuditView {
    return RedeliveryAuditView(
        code = item.code,
        label = truncate(item.label, 120),
        rate = "%.2f".format(item.rate),
        trailerId = item.trailerId,
        routeCode = item.routeCode,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryAuditViews(items: MutableList<RedeliveryAudit>): MutableList<RedeliveryAuditView> {
    val views = mutableListOf<RedeliveryAuditView>()
    for (item in items) {
        views.add(toRedeliveryAuditView(item))
    }
    return views
}

fun sumRedeliveryAuditView(view: RedeliveryAuditView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
