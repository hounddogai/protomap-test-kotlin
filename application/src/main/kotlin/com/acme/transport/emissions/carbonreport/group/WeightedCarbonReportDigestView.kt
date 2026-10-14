package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted carbon report digest. */
data class WeightedCarbonReportDigestView(
    var codeText: String,
    var title: String,
    var timezone: String,
    var unitText: String,
    var ceilingText: String,
    var reviewer: String,
    var rate: String,
    var channel: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toWeightedCarbonReportDigestView(item: WeightedCarbonReportDigest): WeightedCarbonReportDigestView {
    return WeightedCarbonReportDigestView(
        codeText = item.code,
        title = item.title,
        timezone = truncate(item.timezone, 64),
        unitText = item.unit,
        ceilingText = "%.2f".format(item.ceiling),
        reviewer = item.reviewer,
        rate = "%.2f".format(item.rate),
        channel = truncate(item.channel, 80),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCarbonReportDigestViews(items: MutableList<WeightedCarbonReportDigest>): MutableList<WeightedCarbonReportDigestView> {
    val views = mutableListOf<WeightedCarbonReportDigestView>()
    for (item in items) {
        views.add(toWeightedCarbonReportDigestView(item))
    }
    return views
}

fun sumWeightedCarbonReportDigestView(view: WeightedCarbonReportDigestView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCeiling + parsedRate + parsedUpdatedAt.toDouble()
}
