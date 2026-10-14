package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient hub threshold. */
data class LenientHubThresholdView(
    var code: String,
    var nameText: String,
    var label: String,
    var ownerId: String,
    var archived: String,
    var amount: String,
    var statusLabel: String,
)

fun toLenientHubThresholdView(item: LenientHubThreshold): LenientHubThresholdView {
    return LenientHubThresholdView(
        code = item.code,
        nameText = truncate(item.name, 40),
        label = item.label,
        ownerId = item.ownerId,
        archived = "${item.archived}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientHubThresholdViews(items: MutableList<LenientHubThreshold>): MutableList<LenientHubThresholdView> {
    val views = mutableListOf<LenientHubThresholdView>()
    for (item in items) {
        views.add(toLenientHubThresholdView(item))
    }
    return views
}

fun sumLenientHubThresholdView(view: LenientHubThresholdView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
