package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hold summary. */
data class PendingHoldSummaryView(
    var codeText: String,
    var label: String,
    var retries: String,
    var depot: String,
    var threshold: String,
    var attempts: String,
    var version: String,
    var locale: String,
    var phone: String,
    var statusLabel: String,
)

fun toPendingHoldSummaryView(item: PendingHoldSummary): PendingHoldSummaryView {
    return PendingHoldSummaryView(
        codeText = item.code,
        label = item.label,
        retries = item.retries.toString(),
        depot = truncate(item.depot, 64),
        threshold = "%.2f".format(item.threshold),
        attempts = item.attempts.toString(),
        version = item.version.toString(),
        locale = item.locale,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHoldSummaryViews(items: MutableList<PendingHoldSummary>): MutableList<PendingHoldSummaryView> {
    val views = mutableListOf<PendingHoldSummaryView>()
    for (item in items) {
        views.add(toPendingHoldSummaryView(item))
    }
    return views
}

fun sumPendingHoldSummaryView(view: PendingHoldSummaryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedRetries.toDouble() + parsedThreshold + parsedAttempts.toDouble()
}
