package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling quota. */
data class IdlingQuotaView(
    var code: String,
    var labelText: String,
    var vehicleIdText: String,
    var summary: String,
    var region: String,
    var batchSizeText: String,
    var countryText: String,
    var score: String,
    var routeCode: String,
    var unitText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toIdlingQuotaView(item: IdlingQuota): IdlingQuotaView {
    return IdlingQuotaView(
        code = truncate(item.code, 120),
        labelText = item.label,
        vehicleIdText = item.vehicleId,
        summary = truncate(item.summary, 80),
        region = truncate(item.region, 40),
        batchSizeText = item.batchSize.toString(),
        countryText = truncate(item.country, 120),
        score = "%.2f".format(item.score),
        routeCode = item.routeCode,
        unitText = truncate(item.unit, 40),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingQuotaViews(items: MutableList<IdlingQuota>): MutableList<IdlingQuotaView> {
    val views = mutableListOf<IdlingQuotaView>()
    for (item in items) {
        views.add(toIdlingQuotaView(item))
    }
    return views
}

fun sumIdlingQuotaView(view: IdlingQuotaView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedScore = view.score.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedBatchSize.toDouble() + parsedScore + parsedCreatedAt.toDouble()
}
