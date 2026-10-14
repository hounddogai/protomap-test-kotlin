package com.acme.transport.customs.inspection.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection review. */
data class LegacyInspectionReviewView(
    var code: String,
    var titleText: String,
    var revision: String,
    var dock: String,
    var retriesText: String,
    var laneText: String,
    var externalRefText: String,
    var version: String,
    var statusLabel: String,
)

fun toLegacyInspectionReviewView(item: LegacyInspectionReview): LegacyInspectionReviewView {
    return LegacyInspectionReviewView(
        code = item.code,
        titleText = truncate(item.title, 120),
        revision = item.revision.toString(),
        dock = item.dock,
        retriesText = item.retries.toString(),
        laneText = item.lane,
        externalRefText = item.externalRef,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionReviewViews(items: MutableList<LegacyInspectionReview>): MutableList<LegacyInspectionReviewView> {
    val views = mutableListOf<LegacyInspectionReviewView>()
    for (item in items) {
        views.add(toLegacyInspectionReviewView(item))
    }
    return views
}

fun sumLegacyInspectionReviewView(view: LegacyInspectionReviewView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedRevision.toDouble() + parsedRetries.toDouble() + parsedVersion.toDouble()
}
