package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery audit. */
data class SeasonalRedeliveryAuditView(
    var codeText: String,
    var label: String,
    var priority: String,
    var unitText: String,
    var weight: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryAuditView(item: SeasonalRedeliveryAudit): SeasonalRedeliveryAuditView {
    return SeasonalRedeliveryAuditView(
        codeText = item.code,
        label = item.label,
        priority = item.priority.toString(),
        unitText = truncate(item.unit, 40),
        weight = "%.2f".format(item.weight),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryAuditViews(items: MutableList<SeasonalRedeliveryAudit>): MutableList<SeasonalRedeliveryAuditView> {
    val views = mutableListOf<SeasonalRedeliveryAuditView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryAuditView(item))
    }
    return views
}

fun sumSeasonalRedeliveryAuditView(view: SeasonalRedeliveryAuditView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedWeight
}
