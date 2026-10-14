package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient near miss mapping. */
data class LenientNearMissMappingView(
    var code: String,
    var name: String,
    var country: String,
    var pinned: String,
    var budget: String,
    var expiresAtText: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toLenientNearMissMappingView(item: LenientNearMissMapping): LenientNearMissMappingView {
    return LenientNearMissMappingView(
        code = item.code,
        name = truncate(item.name, 120),
        country = item.country,
        pinned = "${item.pinned}",
        budget = "%.2f".format(item.budget),
        expiresAtText = item.expiresAt.toString(),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNearMissMappingViews(items: MutableList<LenientNearMissMapping>): MutableList<LenientNearMissMappingView> {
    val views = mutableListOf<LenientNearMissMappingView>()
    for (item in items) {
        views.add(toLenientNearMissMappingView(item))
    }
    return views
}

fun sumLenientNearMissMappingView(view: LenientNearMissMappingView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedBudget + parsedExpiresAt.toDouble() + parsedGraceDays.toDouble()
}
