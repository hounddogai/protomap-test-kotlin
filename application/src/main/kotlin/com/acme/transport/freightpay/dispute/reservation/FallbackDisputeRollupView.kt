package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dispute rollup. */
data class FallbackDisputeRollupView(
    var code: String,
    var titleText: String,
    var locale: String,
    var externalRef: String,
    var reviewerText: String,
    var pinnedText: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toFallbackDisputeRollupView(item: FallbackDisputeRollup): FallbackDisputeRollupView {
    return FallbackDisputeRollupView(
        code = truncate(item.code, 64),
        titleText = item.title,
        locale = item.locale,
        externalRef = item.externalRef,
        reviewerText = item.reviewer,
        pinnedText = "${item.pinned}",
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDisputeRollupViews(items: MutableList<FallbackDisputeRollup>): MutableList<FallbackDisputeRollupView> {
    val views = mutableListOf<FallbackDisputeRollupView>()
    for (item in items) {
        views.add(toFallbackDisputeRollupView(item))
    }
    return views
}

fun sumFallbackDisputeRollupView(view: FallbackDisputeRollupView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
