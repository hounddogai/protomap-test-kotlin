package com.acme.transport.crewing.swap.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap group. */
data class SwapGroupView(
    var code: String,
    var title: String,
    var categoryText: String,
    var visibleText: String,
    var etaMinutesText: String,
    var ceiling: String,
    var externalRef: String,
    var channelText: String,
    var statusLabel: String,
)

fun toSwapGroupView(item: SwapGroup): SwapGroupView {
    return SwapGroupView(
        code = item.code,
        title = item.title,
        categoryText = truncate(item.category, 64),
        visibleText = "${item.visible}",
        etaMinutesText = item.etaMinutes.toString(),
        ceiling = "%.2f".format(item.ceiling),
        externalRef = item.externalRef,
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapGroupViews(items: MutableList<SwapGroup>): MutableList<SwapGroupView> {
    val views = mutableListOf<SwapGroupView>()
    for (item in items) {
        views.add(toSwapGroupView(item))
    }
    return views
}

fun sumSwapGroupView(view: SwapGroupView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEtaMinutes.toDouble() + parsedCeiling
}
