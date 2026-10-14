package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt bundle. */
data class AttemptBundleView(
    var code: String,
    var name: String,
    var batchSizeText: String,
    var phoneText: String,
    var pinned: String,
    var depot: String,
    var statusLabel: String,
)

fun toAttemptBundleView(item: AttemptBundle): AttemptBundleView {
    return AttemptBundleView(
        code = truncate(item.code, 64),
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        phoneText = truncate(item.phone, 64),
        pinned = "${item.pinned}",
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptBundleViews(items: MutableList<AttemptBundle>): MutableList<AttemptBundleView> {
    val views = mutableListOf<AttemptBundleView>()
    for (item in items) {
        views.add(toAttemptBundleView(item))
    }
    return views
}

fun sumAttemptBundleView(view: AttemptBundleView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedBatchSize.toDouble()
}
