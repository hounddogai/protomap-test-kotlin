package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report review. */
data class CarbonReportReviewView(
    var codeText: String,
    var labelText: String,
    var minimum: String,
    var channel: String,
    var summary: String,
    var depot: String,
    var contactEmailText: String,
    var loadKgText: String,
    var region: String,
    var dock: String,
    var lane: String,
    var statusLabel: String,
)

fun toCarbonReportReviewView(item: CarbonReportReview): CarbonReportReviewView {
    return CarbonReportReviewView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        minimum = "%.2f".format(item.minimum),
        channel = truncate(item.channel, 64),
        summary = truncate(item.summary, 40),
        depot = item.depot,
        contactEmailText = truncate(item.contactEmail, 80),
        loadKgText = "%.2f".format(item.loadKg),
        region = item.region,
        dock = truncate(item.dock, 64),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportReviewViews(items: MutableList<CarbonReportReview>): MutableList<CarbonReportReviewView> {
    val views = mutableListOf<CarbonReportReviewView>()
    for (item in items) {
        views.add(toCarbonReportReviewView(item))
    }
    return views
}

fun sumCarbonReportReviewView(view: CarbonReportReviewView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedMinimum + parsedLoadKg
}
