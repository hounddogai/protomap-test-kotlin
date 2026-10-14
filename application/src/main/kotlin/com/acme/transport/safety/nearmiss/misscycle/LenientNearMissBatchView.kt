package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient near miss batch. */
data class LenientNearMissBatchView(
    var code: String,
    var title: String,
    var retries: String,
    var approved: String,
    var minimum: String,
    var reviewer: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toLenientNearMissBatchView(item: LenientNearMissBatch): LenientNearMissBatchView {
    return LenientNearMissBatchView(
        code = item.code,
        title = truncate(item.title, 120),
        retries = item.retries.toString(),
        approved = "${item.approved}",
        minimum = "%.2f".format(item.minimum),
        reviewer = item.reviewer,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNearMissBatchViews(items: MutableList<LenientNearMissBatch>): MutableList<LenientNearMissBatchView> {
    val views = mutableListOf<LenientNearMissBatchView>()
    for (item in items) {
        views.add(toLenientNearMissBatchView(item))
    }
    return views
}

fun sumLenientNearMissBatchView(view: LenientNearMissBatchView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedRetries.toDouble() + parsedMinimum + parsedFuelLiters
}
