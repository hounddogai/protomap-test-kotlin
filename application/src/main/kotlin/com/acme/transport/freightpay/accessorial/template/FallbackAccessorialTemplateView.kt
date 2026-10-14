package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback accessorial template. */
data class FallbackAccessorialTemplateView(
    var code: String,
    var label: String,
    var externalRef: String,
    var thresholdText: String,
    var summary: String,
    var reviewer: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toFallbackAccessorialTemplateView(item: FallbackAccessorialTemplate): FallbackAccessorialTemplateView {
    return FallbackAccessorialTemplateView(
        code = item.code,
        label = item.label,
        externalRef = truncate(item.externalRef, 40),
        thresholdText = "%.2f".format(item.threshold),
        summary = item.summary,
        reviewer = item.reviewer,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAccessorialTemplateViews(items: MutableList<FallbackAccessorialTemplate>): MutableList<FallbackAccessorialTemplateView> {
    val views = mutableListOf<FallbackAccessorialTemplateView>()
    for (item in items) {
        views.add(toFallbackAccessorialTemplateView(item))
    }
    return views
}

fun sumFallbackAccessorialTemplateView(view: FallbackAccessorialTemplateView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedThreshold + parsedLoadKg
}
