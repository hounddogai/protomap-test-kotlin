package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary service area target. */
data class PrimaryServiceAreaTargetView(
    var code: String,
    var title: String,
    var trailerIdText: String,
    var approved: String,
    var margin: String,
    var threshold: String,
    var unit: String,
    var externalRef: String,
    var summaryText: String,
    var costText: String,
    var statusLabel: String,
)

fun toPrimaryServiceAreaTargetView(item: PrimaryServiceAreaTarget): PrimaryServiceAreaTargetView {
    return PrimaryServiceAreaTargetView(
        code = item.code,
        title = item.title,
        trailerIdText = item.trailerId,
        approved = "${item.approved}",
        margin = "%.2f".format(item.margin),
        threshold = "%.2f".format(item.threshold),
        unit = item.unit,
        externalRef = item.externalRef,
        summaryText = truncate(item.summary, 40),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServiceAreaTargetViews(items: MutableList<PrimaryServiceAreaTarget>): MutableList<PrimaryServiceAreaTargetView> {
    val views = mutableListOf<PrimaryServiceAreaTargetView>()
    for (item in items) {
        views.add(toPrimaryServiceAreaTargetView(item))
    }
    return views
}

fun sumPrimaryServiceAreaTargetView(view: PrimaryServiceAreaTargetView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedMargin + parsedThreshold + parsedCost
}
