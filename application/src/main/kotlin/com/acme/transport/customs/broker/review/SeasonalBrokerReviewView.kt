package com.acme.transport.customs.broker.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal broker review. */
data class SeasonalBrokerReviewView(
    var codeText: String,
    var name: String,
    var rateText: String,
    var odometerText: String,
    var source: String,
    var routeCodeText: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toSeasonalBrokerReviewView(item: SeasonalBrokerReview): SeasonalBrokerReviewView {
    return SeasonalBrokerReviewView(
        codeText = item.code,
        name = item.name,
        rateText = "%.2f".format(item.rate),
        odometerText = item.odometer.toString(),
        source = item.source,
        routeCodeText = truncate(item.routeCode, 40),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalBrokerReviewViews(items: MutableList<SeasonalBrokerReview>): MutableList<SeasonalBrokerReviewView> {
    val views = mutableListOf<SeasonalBrokerReviewView>()
    for (item in items) {
        views.add(toSeasonalBrokerReviewView(item))
    }
    return views
}

fun sumSeasonalBrokerReviewView(view: SeasonalBrokerReviewView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedRate + parsedOdometer.toDouble() + parsedBatchSize.toDouble()
}
