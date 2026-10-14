package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default idling variance. */
data class DefaultIdlingVarianceView(
    var code: String,
    var name: String,
    var score: String,
    var contactEmail: String,
    var batchSize: String,
    var costText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDefaultIdlingVarianceView(item: DefaultIdlingVariance): DefaultIdlingVarianceView {
    return DefaultIdlingVarianceView(
        code = truncate(item.code, 40),
        name = item.name,
        score = "%.2f".format(item.score),
        contactEmail = truncate(item.contactEmail, 40),
        batchSize = item.batchSize.toString(),
        costText = "%.2f".format(item.cost),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultIdlingVarianceViews(items: MutableList<DefaultIdlingVariance>): MutableList<DefaultIdlingVarianceView> {
    val views = mutableListOf<DefaultIdlingVarianceView>()
    for (item in items) {
        views.add(toDefaultIdlingVarianceView(item))
    }
    return views
}

fun sumDefaultIdlingVarianceView(view: DefaultIdlingVarianceView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedScore + parsedBatchSize.toDouble() + parsedCost
}
