package com.acme.transport.crewing.swap.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap audit. */
data class SwapAuditView(
    var code: String,
    var label: String,
    var version: String,
    var quantity: String,
    var externalRef: String,
    var etaMinutesText: String,
    var summaryText: String,
    var graceDays: String,
    var maximum: String,
    var pinnedText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toSwapAuditView(item: SwapAudit): SwapAuditView {
    return SwapAuditView(
        code = item.code,
        label = item.label,
        version = item.version.toString(),
        quantity = item.quantity.toString(),
        externalRef = truncate(item.externalRef, 64),
        etaMinutesText = item.etaMinutes.toString(),
        summaryText = truncate(item.summary, 80),
        graceDays = item.graceDays.toString(),
        maximum = "%.2f".format(item.maximum),
        pinnedText = "${item.pinned}",
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapAuditViews(items: MutableList<SwapAudit>): MutableList<SwapAuditView> {
    val views = mutableListOf<SwapAuditView>()
    for (item in items) {
        views.add(toSwapAuditView(item))
    }
    return views
}

fun sumSwapAuditView(view: SwapAuditView): Double {
    val parsedVersion = view.version.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedVersion.toDouble() + parsedQuantity.toDouble() + parsedEtaMinutes.toDouble()
}
