package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default roster review. */
data class DefaultRosterReviewView(
    var code: String,
    var titleText: String,
    var ceiling: String,
    var notes: String,
    var shiftCode: String,
    var rate: String,
    var graceDays: String,
    var updatedAtText: String,
    var externalRef: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDefaultRosterReviewView(item: DefaultRosterReview): DefaultRosterReviewView {
    return DefaultRosterReviewView(
        code = item.code,
        titleText = item.title,
        ceiling = "%.2f".format(item.ceiling),
        notes = item.notes,
        shiftCode = truncate(item.shiftCode, 64),
        rate = "%.2f".format(item.rate),
        graceDays = item.graceDays.toString(),
        updatedAtText = item.updatedAt.toString(),
        externalRef = item.externalRef,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRosterReviewViews(items: MutableList<DefaultRosterReview>): MutableList<DefaultRosterReviewView> {
    val views = mutableListOf<DefaultRosterReviewView>()
    for (item in items) {
        views.add(toDefaultRosterReviewView(item))
    }
    return views
}

fun sumDefaultRosterReviewView(view: DefaultRosterReviewView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCeiling + parsedRate + parsedGraceDays.toDouble()
}
