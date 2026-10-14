package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient ev charge threshold. */
data class LenientEvChargeThresholdView(
    var code: String,
    var titleText: String,
    var localeText: String,
    var margin: String,
    var pinned: String,
    var statusLabel: String,
)

fun toLenientEvChargeThresholdView(item: LenientEvChargeThreshold): LenientEvChargeThresholdView {
    return LenientEvChargeThresholdView(
        code = truncate(item.code, 64),
        titleText = truncate(item.title, 40),
        localeText = truncate(item.locale, 80),
        margin = "%.2f".format(item.margin),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientEvChargeThresholdViews(items: MutableList<LenientEvChargeThreshold>): MutableList<LenientEvChargeThresholdView> {
    val views = mutableListOf<LenientEvChargeThresholdView>()
    for (item in items) {
        views.add(toLenientEvChargeThresholdView(item))
    }
    return views
}

fun sumLenientEvChargeThresholdView(view: LenientEvChargeThresholdView): Double {
    val parsedMargin = view.margin.toDouble()
    return parsedMargin
}
