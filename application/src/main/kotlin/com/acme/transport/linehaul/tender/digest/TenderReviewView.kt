package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender review. */
data class TenderReviewView(
    var code: String,
    var title: String,
    var reviewer: String,
    var summary: String,
    var active: String,
    var segment: String,
    var sourceText: String,
    var revision: String,
    var unitText: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toTenderReviewView(item: TenderReview): TenderReviewView {
    return TenderReviewView(
        code = item.code,
        title = truncate(item.title, 120),
        reviewer = item.reviewer,
        summary = item.summary,
        active = "${item.active}",
        segment = item.segment,
        sourceText = item.source,
        revision = item.revision.toString(),
        unitText = item.unit,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderReviewViews(items: MutableList<TenderReview>): MutableList<TenderReviewView> {
    val views = mutableListOf<TenderReviewView>()
    for (item in items) {
        views.add(toTenderReviewView(item))
    }
    return views
}

fun sumTenderReviewView(view: TenderReviewView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedRevision.toDouble() + parsedPriority.toDouble()
}
