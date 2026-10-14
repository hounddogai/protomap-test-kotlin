package com.acme.transport.customs.inspection.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection review. */
data class AdaptiveInspectionReviewView(
    var codeText: String,
    var name: String,
    var notes: String,
    var externalRef: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionReviewView(item: AdaptiveInspectionReview): AdaptiveInspectionReviewView {
    return AdaptiveInspectionReviewView(
        codeText = item.code,
        name = truncate(item.name, 40),
        notes = truncate(item.notes, 120),
        externalRef = item.externalRef,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionReviewViews(items: MutableList<AdaptiveInspectionReview>): MutableList<AdaptiveInspectionReviewView> {
    val views = mutableListOf<AdaptiveInspectionReviewView>()
    for (item in items) {
        views.add(toAdaptiveInspectionReviewView(item))
    }
    return views
}

fun sumAdaptiveInspectionReviewView(view: AdaptiveInspectionReviewView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEndsAt.toDouble()
}
