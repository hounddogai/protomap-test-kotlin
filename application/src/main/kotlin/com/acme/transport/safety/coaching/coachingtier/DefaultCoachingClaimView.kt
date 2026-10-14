package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default coaching claim. */
data class DefaultCoachingClaimView(
    var codeText: String,
    var titleText: String,
    var quantity: String,
    var loadKg: String,
    var summaryText: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toDefaultCoachingClaimView(item: DefaultCoachingClaim): DefaultCoachingClaimView {
    return DefaultCoachingClaimView(
        codeText = truncate(item.code, 120),
        titleText = item.title,
        quantity = item.quantity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        summaryText = truncate(item.summary, 120),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCoachingClaimViews(items: MutableList<DefaultCoachingClaim>): MutableList<DefaultCoachingClaimView> {
    val views = mutableListOf<DefaultCoachingClaimView>()
    for (item in items) {
        views.add(toDefaultCoachingClaimView(item))
    }
    return views
}

fun sumDefaultCoachingClaimView(view: DefaultCoachingClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedQuantity.toDouble() + parsedLoadKg + parsedSequence.toDouble()
}
