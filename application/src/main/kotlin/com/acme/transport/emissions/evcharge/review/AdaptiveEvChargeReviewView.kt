package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive ev charge review. */
data class AdaptiveEvChargeReviewView(
    var code: String,
    var label: String,
    var activeText: String,
    var loadKg: String,
    var segment: String,
    var retriesText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toAdaptiveEvChargeReviewView(item: AdaptiveEvChargeReview): AdaptiveEvChargeReviewView {
    return AdaptiveEvChargeReviewView(
        code = item.code,
        label = truncate(item.label, 64),
        activeText = "${item.active}",
        loadKg = "%.2f".format(item.loadKg),
        segment = item.segment,
        retriesText = item.retries.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEvChargeReviewViews(items: MutableList<AdaptiveEvChargeReview>): MutableList<AdaptiveEvChargeReviewView> {
    val views = mutableListOf<AdaptiveEvChargeReviewView>()
    for (item in items) {
        views.add(toAdaptiveEvChargeReviewView(item))
    }
    return views
}

fun sumAdaptiveEvChargeReviewView(view: AdaptiveEvChargeReviewView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedLoadKg + parsedRetries.toDouble() + parsedEndsAt.toDouble()
}
