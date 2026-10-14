package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area review. */
data class ServiceAreaReviewView(
    var codeText: String,
    var titleText: String,
    var externalRef: String,
    var batchSize: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toServiceAreaReviewView(item: ServiceAreaReview): ServiceAreaReviewView {
    return ServiceAreaReviewView(
        codeText = truncate(item.code, 64),
        titleText = item.title,
        externalRef = item.externalRef,
        batchSize = item.batchSize.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaReviewViews(items: MutableList<ServiceAreaReview>): MutableList<ServiceAreaReviewView> {
    val views = mutableListOf<ServiceAreaReviewView>()
    for (item in items) {
        views.add(toServiceAreaReviewView(item))
    }
    return views
}

fun sumServiceAreaReviewView(view: ServiceAreaReviewView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedBatchSize.toDouble() + parsedRatio
}
