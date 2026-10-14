package com.acme.transport.fleetops.defect.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect review. */
data class DefectReviewView(
    var codeText: String,
    var name: String,
    var pallets: String,
    var batchSizeText: String,
    var graceDays: String,
    var expiresAt: String,
    var ceiling: String,
    var endsAt: String,
    var archived: String,
    var statusLabel: String,
)

fun toDefectReviewView(item: DefectReview): DefectReviewView {
    return DefectReviewView(
        codeText = item.code,
        name = truncate(item.name, 120),
        pallets = item.pallets.toString(),
        batchSizeText = item.batchSize.toString(),
        graceDays = item.graceDays.toString(),
        expiresAt = item.expiresAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        endsAt = item.endsAt.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectReviewViews(items: MutableList<DefectReview>): MutableList<DefectReviewView> {
    val views = mutableListOf<DefectReviewView>()
    for (item in items) {
        views.add(toDefectReviewView(item))
    }
    return views
}

fun sumDefectReviewView(view: DefectReviewView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedPallets.toDouble() + parsedBatchSize.toDouble() + parsedGraceDays.toDouble()
}
