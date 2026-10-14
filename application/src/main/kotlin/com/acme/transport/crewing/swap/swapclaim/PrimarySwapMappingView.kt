package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary swap mapping. */
data class PrimarySwapMappingView(
    var codeText: String,
    var titleText: String,
    var carrier: String,
    var score: String,
    var externalRef: String,
    var phone: String,
    var active: String,
    var maximum: String,
    var marginText: String,
    var statusLabel: String,
)

fun toPrimarySwapMappingView(item: PrimarySwapMapping): PrimarySwapMappingView {
    return PrimarySwapMappingView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        carrier = item.carrier,
        score = "%.2f".format(item.score),
        externalRef = item.externalRef,
        phone = item.phone,
        active = "${item.active}",
        maximum = "%.2f".format(item.maximum),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimarySwapMappingViews(items: MutableList<PrimarySwapMapping>): MutableList<PrimarySwapMappingView> {
    val views = mutableListOf<PrimarySwapMappingView>()
    for (item in items) {
        views.add(toPrimarySwapMappingView(item))
    }
    return views
}

fun sumPrimarySwapMappingView(view: PrimarySwapMappingView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedScore + parsedMaximum + parsedMargin
}
