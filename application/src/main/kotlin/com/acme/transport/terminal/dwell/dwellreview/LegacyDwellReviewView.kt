package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy dwell review. */
data class LegacyDwellReviewView(
    var code: String,
    var label: String,
    var batchSize: String,
    var pallets: String,
    var retries: String,
    var routeCode: String,
    var unit: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toLegacyDwellReviewView(item: LegacyDwellReview): LegacyDwellReviewView {
    return LegacyDwellReviewView(
        code = item.code,
        label = item.label,
        batchSize = item.batchSize.toString(),
        pallets = item.pallets.toString(),
        retries = item.retries.toString(),
        routeCode = truncate(item.routeCode, 120),
        unit = item.unit,
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDwellReviewViews(items: MutableList<LegacyDwellReview>): MutableList<LegacyDwellReviewView> {
    val views = mutableListOf<LegacyDwellReviewView>()
    for (item in items) {
        views.add(toLegacyDwellReviewView(item))
    }
    return views
}

fun sumLegacyDwellReviewView(view: LegacyDwellReviewView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedBatchSize.toDouble() + parsedPallets.toDouble() + parsedRetries.toDouble()
}
