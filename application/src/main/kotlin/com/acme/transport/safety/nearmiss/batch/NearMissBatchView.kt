package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss batch. */
data class NearMissBatchView(
    var code: String,
    var title: String,
    var expiresAtText: String,
    var amount: String,
    var loadKg: String,
    var scoreText: String,
    var pinnedText: String,
    var lane: String,
    var createdAt: String,
    var threshold: String,
    var statusLabel: String,
)

fun toNearMissBatchView(item: NearMissBatch): NearMissBatchView {
    return NearMissBatchView(
        code = truncate(item.code, 64),
        title = item.title,
        expiresAtText = item.expiresAt.toString(),
        amount = "%.2f".format(item.amount),
        loadKg = "%.2f".format(item.loadKg),
        scoreText = "%.2f".format(item.score),
        pinnedText = "${item.pinned}",
        lane = item.lane,
        createdAt = item.createdAt.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissBatchViews(items: MutableList<NearMissBatch>): MutableList<NearMissBatchView> {
    val views = mutableListOf<NearMissBatchView>()
    for (item in items) {
        views.add(toNearMissBatchView(item))
    }
    return views
}

fun sumNearMissBatchView(view: NearMissBatchView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedExpiresAt.toDouble() + parsedAmount + parsedLoadKg
}
