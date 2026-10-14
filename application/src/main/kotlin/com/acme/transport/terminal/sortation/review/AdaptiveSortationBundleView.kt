package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive sortation bundle. */
data class AdaptiveSortationBundleView(
    var code: String,
    var label: String,
    var graceDaysText: String,
    var channel: String,
    var externalRef: String,
    var sequence: String,
    var statusLabel: String,
)

fun toAdaptiveSortationBundleView(item: AdaptiveSortationBundle): AdaptiveSortationBundleView {
    return AdaptiveSortationBundleView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 80),
        graceDaysText = item.graceDays.toString(),
        channel = item.channel,
        externalRef = item.externalRef,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSortationBundleViews(items: MutableList<AdaptiveSortationBundle>): MutableList<AdaptiveSortationBundleView> {
    val views = mutableListOf<AdaptiveSortationBundleView>()
    for (item in items) {
        views.add(toAdaptiveSortationBundleView(item))
    }
    return views
}

fun sumAdaptiveSortationBundleView(view: AdaptiveSortationBundleView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedGraceDays.toDouble() + parsedSequence.toDouble()
}
