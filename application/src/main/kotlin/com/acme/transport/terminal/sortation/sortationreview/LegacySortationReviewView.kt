package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy sortation review. */
data class LegacySortationReviewView(
    var codeText: String,
    var title: String,
    var limitText: String,
    var expiresAt: String,
    var revision: String,
    var sequence: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toLegacySortationReviewView(item: LegacySortationReview): LegacySortationReviewView {
    return LegacySortationReviewView(
        codeText = truncate(item.code, 120),
        title = item.title,
        limitText = item.limit.toString(),
        expiresAt = item.expiresAt.toString(),
        revision = item.revision.toString(),
        sequence = item.sequence.toString(),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacySortationReviewViews(items: MutableList<LegacySortationReview>): MutableList<LegacySortationReviewView> {
    val views = mutableListOf<LegacySortationReviewView>()
    for (item in items) {
        views.add(toLegacySortationReviewView(item))
    }
    return views
}

fun sumLegacySortationReviewView(view: LegacySortationReviewView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedLimit.toDouble() + parsedExpiresAt.toDouble() + parsedRevision.toDouble()
}
