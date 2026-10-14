package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback node estimate. */
data class FallbackNodeEstimateView(
    var codeText: String,
    var name: String,
    var expiresAt: String,
    var durationMinutes: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toFallbackNodeEstimateView(item: FallbackNodeEstimate): FallbackNodeEstimateView {
    return FallbackNodeEstimateView(
        codeText = item.code,
        name = truncate(item.name, 120),
        expiresAt = item.expiresAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackNodeEstimateViews(items: MutableList<FallbackNodeEstimate>): MutableList<FallbackNodeEstimateView> {
    val views = mutableListOf<FallbackNodeEstimateView>()
    for (item in items) {
        views.add(toFallbackNodeEstimateView(item))
    }
    return views
}

fun sumFallbackNodeEstimateView(view: FallbackNodeEstimateView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedDurationMinutes.toDouble()
}
