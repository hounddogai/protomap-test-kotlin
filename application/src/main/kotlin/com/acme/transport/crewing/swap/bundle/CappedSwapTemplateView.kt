package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped swap template. */
data class CappedSwapTemplateView(
    var code: String,
    var name: String,
    var weightText: String,
    var sequenceText: String,
    var ratio: String,
    var archived: String,
    var statusLabel: String,
)

fun toCappedSwapTemplateView(item: CappedSwapTemplate): CappedSwapTemplateView {
    return CappedSwapTemplateView(
        code = item.code,
        name = truncate(item.name, 64),
        weightText = "%.2f".format(item.weight),
        sequenceText = item.sequence.toString(),
        ratio = "%.2f".format(item.ratio),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedSwapTemplateViews(items: MutableList<CappedSwapTemplate>): MutableList<CappedSwapTemplateView> {
    val views = mutableListOf<CappedSwapTemplateView>()
    for (item in items) {
        views.add(toCappedSwapTemplateView(item))
    }
    return views
}

fun sumCappedSwapTemplateView(view: CappedSwapTemplateView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedWeight + parsedSequence.toDouble() + parsedRatio
}
