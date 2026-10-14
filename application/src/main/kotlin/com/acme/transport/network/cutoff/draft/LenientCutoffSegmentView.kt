package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient cutoff segment. */
data class LenientCutoffSegmentView(
    var codeText: String,
    var labelText: String,
    var reviewerText: String,
    var countryText: String,
    var name: String,
    var dock: String,
    var quantity: String,
    var statusLabel: String,
)

fun toLenientCutoffSegmentView(item: LenientCutoffSegment): LenientCutoffSegmentView {
    return LenientCutoffSegmentView(
        codeText = item.code,
        labelText = item.label,
        reviewerText = truncate(item.reviewer, 64),
        countryText = item.country,
        name = item.name,
        dock = item.dock,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCutoffSegmentViews(items: MutableList<LenientCutoffSegment>): MutableList<LenientCutoffSegmentView> {
    val views = mutableListOf<LenientCutoffSegmentView>()
    for (item in items) {
        views.add(toLenientCutoffSegmentView(item))
    }
    return views
}

fun sumLenientCutoffSegmentView(view: LenientCutoffSegmentView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
