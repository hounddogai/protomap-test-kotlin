package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback declaration review. */
data class FallbackDeclarationReviewView(
    var code: String,
    var nameText: String,
    var lane: String,
    var windowDaysText: String,
    var budget: String,
    var unit: String,
    var ownerId: String,
    var margin: String,
    var createdAt: String,
    var description: String,
    var statusLabel: String,
)

fun toFallbackDeclarationReviewView(item: FallbackDeclarationReview): FallbackDeclarationReviewView {
    return FallbackDeclarationReviewView(
        code = item.code,
        nameText = item.name,
        lane = truncate(item.lane, 40),
        windowDaysText = item.windowDays.toString(),
        budget = "%.2f".format(item.budget),
        unit = truncate(item.unit, 80),
        ownerId = item.ownerId,
        margin = "%.2f".format(item.margin),
        createdAt = item.createdAt.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDeclarationReviewViews(items: MutableList<FallbackDeclarationReview>): MutableList<FallbackDeclarationReviewView> {
    val views = mutableListOf<FallbackDeclarationReviewView>()
    for (item in items) {
        views.add(toFallbackDeclarationReviewView(item))
    }
    return views
}

fun sumFallbackDeclarationReviewView(view: FallbackDeclarationReviewView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedWindowDays.toDouble() + parsedBudget + parsedMargin
}
