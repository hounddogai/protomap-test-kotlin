package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback lane plan review. */
data class FallbackLanePlanReviewView(
    var codeText: String,
    var label: String,
    var active: String,
    var currency: String,
    var priorityText: String,
    var timezone: String,
    var archived: String,
    var attempts: String,
    var driverId: String,
    var statusLabel: String,
)

fun toFallbackLanePlanReviewView(item: FallbackLanePlanReview): FallbackLanePlanReviewView {
    return FallbackLanePlanReviewView(
        codeText = item.code,
        label = truncate(item.label, 120),
        active = "${item.active}",
        currency = item.currency,
        priorityText = item.priority.toString(),
        timezone = item.timezone,
        archived = "${item.archived}",
        attempts = item.attempts.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLanePlanReviewViews(items: MutableList<FallbackLanePlanReview>): MutableList<FallbackLanePlanReviewView> {
    val views = mutableListOf<FallbackLanePlanReviewView>()
    for (item in items) {
        views.add(toFallbackLanePlanReviewView(item))
    }
    return views
}

fun sumFallbackLanePlanReviewView(view: FallbackLanePlanReviewView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedPriority.toDouble() + parsedAttempts.toDouble()
}
