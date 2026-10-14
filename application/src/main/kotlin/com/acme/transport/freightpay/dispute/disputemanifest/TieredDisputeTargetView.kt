package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered dispute target. */
data class TieredDisputeTargetView(
    var codeText: String,
    var label: String,
    var dock: String,
    var attempts: String,
    var unit: String,
    var statusLabel: String,
)

fun toTieredDisputeTargetView(item: TieredDisputeTarget): TieredDisputeTargetView {
    return TieredDisputeTargetView(
        codeText = item.code,
        label = truncate(item.label, 80),
        dock = item.dock,
        attempts = item.attempts.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDisputeTargetViews(items: MutableList<TieredDisputeTarget>): MutableList<TieredDisputeTargetView> {
    val views = mutableListOf<TieredDisputeTargetView>()
    for (item in items) {
        views.add(toTieredDisputeTargetView(item))
    }
    return views
}

fun sumTieredDisputeTargetView(view: TieredDisputeTargetView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
