package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break template. */
data class RestBreakTemplateView(
    var code: String,
    var name: String,
    var dock: String,
    var currencyText: String,
    var score: String,
    var weightText: String,
    var statusLabel: String,
)

fun toRestBreakTemplateView(item: RestBreakTemplate): RestBreakTemplateView {
    return RestBreakTemplateView(
        code = item.code,
        name = item.name,
        dock = item.dock,
        currencyText = truncate(item.currency, 40),
        score = "%.2f".format(item.score),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakTemplateViews(items: MutableList<RestBreakTemplate>): MutableList<RestBreakTemplateView> {
    val views = mutableListOf<RestBreakTemplateView>()
    for (item in items) {
        views.add(toRestBreakTemplateView(item))
    }
    return views
}

fun sumRestBreakTemplateView(view: RestBreakTemplateView): Double {
    val parsedScore = view.score.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedScore + parsedWeight
}
