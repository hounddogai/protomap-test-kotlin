package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap template. */
data class SwapTemplateView(
    var codeText: String,
    var title: String,
    var timezoneText: String,
    var depot: String,
    var reviewer: String,
    var score: String,
    var statusLabel: String,
)

fun toSwapTemplateView(item: SwapTemplate): SwapTemplateView {
    return SwapTemplateView(
        codeText = item.code,
        title = item.title,
        timezoneText = item.timezone,
        depot = truncate(item.depot, 40),
        reviewer = item.reviewer,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapTemplateViews(items: MutableList<SwapTemplate>): MutableList<SwapTemplateView> {
    val views = mutableListOf<SwapTemplateView>()
    for (item in items) {
        views.add(toSwapTemplateView(item))
    }
    return views
}

fun sumSwapTemplateView(view: SwapTemplateView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
