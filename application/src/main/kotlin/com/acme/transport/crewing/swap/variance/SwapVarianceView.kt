package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap variance. */
data class SwapVarianceView(
    var code: String,
    var title: String,
    var margin: String,
    var pallets: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toSwapVarianceView(item: SwapVariance): SwapVarianceView {
    return SwapVarianceView(
        code = item.code,
        title = truncate(item.title, 80),
        margin = "%.2f".format(item.margin),
        pallets = item.pallets.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapVarianceViews(items: MutableList<SwapVariance>): MutableList<SwapVarianceView> {
    val views = mutableListOf<SwapVarianceView>()
    for (item in items) {
        views.add(toSwapVarianceView(item))
    }
    return views
}

fun sumSwapVarianceView(view: SwapVarianceView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMargin + parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
